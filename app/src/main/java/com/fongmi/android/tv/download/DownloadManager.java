package com.fongmi.android.tv.download;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.SiteApi;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Episode;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.Task;
import com.github.catvod.net.OkHttp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Response;

public class DownloadManager {

    private static DownloadManager sInstance;
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final int TIMEOUT = 30000;

    private final ConcurrentHashMap<String, DownloadTask> mTasks = new ConcurrentHashMap<>();
    private final Context mContext;
    private String mDownloadPath;
    private DownloadListener mListener;

    public interface DownloadListener {
        void onProgress(String key, int progress, long downloaded, long total);
        void onComplete(String key, File file);
        void onError(String key, String error);
    }

    public static class DownloadTask {
        public String key;
        public String url;
        public String name;
        public String savePath;
        public Map<String, String> headers;
        public int progress;
        public long downloaded;
        public long total;
        public boolean downloading;
        public boolean cancelled;
        public Thread thread;
        public File outputFile;

        public DownloadTask(String key, String url, String name, String savePath, Map<String, String> headers) {
            this.key = key;
            this.url = url;
            this.name = name;
            this.savePath = savePath;
            this.headers = headers;
        }
    }

    private DownloadManager(Context context) {
        mContext = context.getApplicationContext();
        mDownloadPath = getDefaultDownloadPath();
    }

    public static synchronized DownloadManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DownloadManager(context);
        }
        return sInstance;
    }

    public void setListener(DownloadListener listener) {
        mListener = listener;
    }

    public void setDownloadPath(String path) {
        mDownloadPath = path;
        if (!mDownloadPath.endsWith("/")) {
            mDownloadPath += "/";
        }
    }

    public String getDownloadPath() {
        return mDownloadPath;
    }

    private String getDefaultDownloadPath() {
        File dir = App.get().getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (dir != null) {
            return dir.getAbsolutePath() + "/TVBox/";
        }
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/TVBox/";
    }

    private String sanitizeFileName(String name) {
        if (TextUtils.isEmpty(name)) {
            name = "video";
        }
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        return name.trim();
    }

    public void downloadVideo(String url, Map<String, String> headers, String title, String episodeName) {
        if (TextUtils.isEmpty(url)) {
            Notify.show(R.string.download_invalid_url);
            return;
        }

        String key = url.hashCode() + "";
        if (mTasks.containsKey(key) && mTasks.get(key).downloading) {
            Notify.show("正在下载中...");
            return;
        }

        String fileName = sanitizeFileName(title);
        if (!TextUtils.isEmpty(episodeName)) {
            fileName += "_" + sanitizeFileName(episodeName);
        }
        fileName += ".mp4";

        String savePath = mDownloadPath + fileName;

        DownloadTask task = new DownloadTask(key, url, fileName, savePath, headers);
        mTasks.put(key, task);

        Task.execute(() -> startDownload(task));
    }

    public void downloadEpisode(String siteKey, String flag, Episode episode, String title) {
        if (episode == null || TextUtils.isEmpty(episode.getUrl())) {
            Notify.show(R.string.download_invalid_url);
            return;
        }

        Task.execute(() -> {
            try {
                Result result = SiteApi.playerContent(siteKey, flag, episode.getUrl());
                if (result == null || TextUtils.isEmpty(result.getRealUrl())) {
                    notifyErrorMain("获取视频地址失败");
                    return;
                }

                if (result.needParse()) {
                    notifyErrorMain(App.get().getString(R.string.download_need_parse));
                    return;
                }

                String realUrl = result.getRealUrl();
                Map<String, String> headers = result.getHeader();

                String key = realUrl.hashCode() + "";
                if (mTasks.containsKey(key) && mTasks.get(key).downloading) {
                    notifyMainThread(() -> Notify.show("正在下载中..."));
                    return;
                }

                String fileName = sanitizeFileName(title);
                if (!TextUtils.isEmpty(episode.getName())) {
                    fileName += "_" + sanitizeFileName(episode.getName());
                }
                fileName += ".mp4";

                String savePath = mDownloadPath + fileName;

                DownloadTask task = new DownloadTask(key, realUrl, fileName, savePath, headers);
                mTasks.put(key, task);

                notifyMainThread(() -> Notify.show(App.get().getString(R.string.download_start, episode.getName())));

                startDownload(task);

            } catch (Exception e) {
                e.printStackTrace();
                notifyErrorMain("下载失败: " + e.getMessage());
            }
        });
    }

    private void startDownload(DownloadTask task) {
        task.downloading = true;
        task.thread = Thread.currentThread();

        try {
            File dir = new File(mDownloadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            task.outputFile = new File(task.savePath);
            if (task.outputFile.exists()) {
                task.outputFile.delete();
            }

            downloadWithOkHttp(task);

        } catch (Exception e) {
            e.printStackTrace();
            task.downloading = false;
            final String error = e.getMessage();
            notifyMainThread(() -> {
                if (mListener != null) {
                    mListener.onError(task.key, error);
                }
                Notify.show(App.get().getString(R.string.download_error, error));
            });
        } finally {
            mTasks.remove(task.key);
        }
    }

    private void downloadWithOkHttp(DownloadTask task) throws IOException {
        try (Response response = OkHttp.newCall(task.url, task.headers).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP Error: " + response.code());
            }

            long contentLength = 0;
            String contentLengthHeader = response.header("Content-Length");
            if (contentLengthHeader != null) {
                try {
                    contentLength = Long.parseLong(contentLengthHeader);
                } catch (NumberFormatException ignored) {
                }
            }
            task.total = contentLength;

            try (InputStream input = response.body().byteStream();
                 FileOutputStream output = new FileOutputStream(task.outputFile)) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalRead = 0;

                while (!task.cancelled && (bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                    task.downloaded = totalRead;

                    if (task.total > 0) {
                        task.progress = (int) ((totalRead * 100) / task.total);
                    }

                    if (mListener != null) {
                        final int progress = task.progress;
                        final long downloaded = task.downloaded;
                        final long total = task.total;
                        notifyMainThread(() -> mListener.onProgress(task.key, progress, downloaded, total));
                    }
                }

                output.flush();

                if (!task.cancelled) {
                    task.downloading = false;
                    final File file = task.outputFile;
                    notifyMainThread(() -> {
                        if (mListener != null) {
                            mListener.onComplete(task.key, file);
                        }
                        Notify.show(App.get().getString(R.string.download_complete, task.name));
                    });
                }
            }
        }
    }

    public void cancelDownload(String url) {
        String key = url.hashCode() + "";
        DownloadTask task = mTasks.get(key);
        if (task != null) {
            task.cancelled = true;
            if (task.thread != null) {
                task.thread.interrupt();
            }
            mTasks.remove(key);
            Notify.show("已取消下载");
        }
    }

    public boolean isDownloading(String url) {
        String key = url.hashCode() + "";
        DownloadTask task = mTasks.get(key);
        return task != null && task.downloading;
    }

    public int getProgress(String url) {
        String key = url.hashCode() + "";
        DownloadTask task = mTasks.get(key);
        return task != null ? task.progress : 0;
    }

    private void notifyMainThread(Runnable runnable) {
        App.post(runnable, 0);
    }

    private void notifyErrorMain(String error) {
        notifyMainThread(() -> Notify.show(error));
    }
}

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
import com.fongmi.android.tv.databinding.ActivityVideoBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.model.SiteViewModel;
import com.fongmi.android.tv.player.PlayerManager;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.NoOpCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;

public class DownloadManager {

    private static DownloadManager sInstance;
    private static final String CACHE_DIR = "download_cache";
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final int TIMEOUT = 30000;

    private final ConcurrentHashMap<String, DownloadTask> mTasks = new ConcurrentHashMap<>();
    private final Context mContext;
    private SimpleCache mCache;
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
        public int progress;
        public long downloaded;
        public long total;
        public boolean downloading;
        public boolean cancelled;
        public Thread thread;
        public File outputFile;

        public DownloadTask(String key, String url, String name, String savePath) {
            this.key = key;
            this.url = url;
            this.name = name;
            this.savePath = savePath;
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
        // 移除或替换非法字符
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        return name.trim();
    }

    public void downloadVideo(String url, String title, String episodeName) {
        if (TextUtils.isEmpty(url)) {
            Notify.show("视频地址无效");
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

        DownloadTask task = new DownloadTask(key, url, fileName, savePath);
        mTasks.put(key, task);

        Task.execute(() -> startDownload(task));
    }

    private void startDownload(DownloadTask task) {
        task.downloading = true;
        task.thread = Thread.currentThread();

        try {
            // 确保目录存在
            File dir = new File(mDownloadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            URL url = new URL(task.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 9; Build/PPR1.180610.011) AppleWebKit/537.36");
            connection.setRequestProperty("Referer", "https://github.com/m3325599/TV");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP Error: " + responseCode);
            }

            task.total = connection.getContentLength();
            if (task.total <= 0) {
                task.total = 0; // 未知大小
            }

            task.outputFile = new File(task.savePath);

            try (InputStream input = connection.getInputStream();
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

                    // 更新进度
                    if (mListener != null) {
                        notifyProgress(task);
                    }
                }

                output.flush();

                if (!task.cancelled) {
                    task.downloading = false;
                    if (mListener != null) {
                        notifyComplete(task);
                    }
                    notifyMainThread(() -> {
                        if (mListener != null) {
                            mListener.onComplete(task.key, task.outputFile);
                        }
                        Notify.show("下载完成: " + task.name);
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            task.downloading = false;
            final String error = e.getMessage();
            notifyMainThread(() -> {
                if (mListener != null) {
                    mListener.onError(task.key, error);
                }
                Notify.show("下载失败: " + error);
            });
        } finally {
            mTasks.remove(task.key);
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

    private void notifyProgress(DownloadTask task) {
        notifyMainThread(() -> {
            if (mListener != null) {
                mListener.onProgress(task.key, task.progress, task.downloaded, task.total);
            }
        });
    }

    private void notifyComplete(DownloadTask task) {
        notifyMainThread(() -> {
            if (mListener != null) {
                mListener.onComplete(task.key, task.outputFile);
            }
        });
    }

    private void notifyMainThread(Runnable runnable) {
        App.post(runnable, 0);
    }

    public void clearCache() {
        if (mCache != null) {
            mCache.release();
            mCache = null;
        }
    }
}

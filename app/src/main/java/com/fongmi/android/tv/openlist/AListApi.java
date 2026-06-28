package com.fongmi.android.tv.openlist;

import android.text.TextUtils;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.utils.Task;
import com.github.catvod.net.OkHttp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;

public class AListApi {

    private String serverUrl;
    private String token;

    public AListApi(String serverUrl, String token) {
        this.token = token;
        setServerUrl(serverUrl);
    }

    public void setServerUrl(String url) {
        this.serverUrl = url;
        if (!TextUtils.isEmpty(url) && !url.endsWith("/")) {
            this.serverUrl = url + "/";
        }
    }

    public void setToken(String token) {
        this.token = token;
    }

    private String getAuthHeader() {
        if (TextUtils.isEmpty(token)) return "";
        if (token.startsWith("Bearer ")) return token;
        return "Bearer " + token;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public interface LoginCallback {
        void onSuccess(String token);
        void onError(String error);
    }

    public void login(String username, String password, LoginCallback callback) {
        Task.execute(() -> {
            try {
                String url = serverUrl + "api/auth/login";
                JSONObject body = new JSONObject();
                body.put("username", username);
                body.put("password", password);

                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json"),
                        body.toString()
                );

                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();

                Response response = OkHttp.client().newCall(request).execute();
                String result = response.body().string();

                JSONObject json = new JSONObject(result);
                int code = json.getInt("code");
                if (code != 200) {
                    String msg = json.optString("message", "Login failed");
                    App.post(() -> callback.onError(msg));
                    return;
                }

                JSONObject data = json.getJSONObject("data");
                String token = data.getString("token");
                App.post(() -> callback.onSuccess(token));

            } catch (Exception e) {
                e.printStackTrace();
                App.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public interface ListCallback {
        void onSuccess(List<AListFile> files);
        void onError(String error);
    }

    public void listFiles(String path, ListCallback callback) {
        Task.execute(() -> {
            try {
                String url = serverUrl + "api/fs/list";
                JSONObject body = new JSONObject();
                body.put("path", path);
                body.put("password", "");
                body.put("page", 1);
                body.put("per_page", 100);
                body.put("refresh", false);

                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json"),
                        body.toString()
                );

                Request.Builder builder = new Request.Builder()
                        .url(url)
                        .post(requestBody);

                if (!TextUtils.isEmpty(token)) {
                    builder.addHeader("Authorization", getAuthHeader());
                }

                Response response = OkHttp.client().newCall(builder.build()).execute();
                String result = response.body().string();

                JSONObject json = new JSONObject(result);
                int code = json.getInt("code");
                if (code != 200) {
                    String msg = json.optString("message", "Unknown error");
                    App.post(() -> callback.onError(msg));
                    return;
                }

                JSONObject data = json.getJSONObject("data");
                JSONArray content = data.getJSONArray("content");

                List<AListFile> files = new ArrayList<>();
                for (int i = 0; i < content.length(); i++) {
                    JSONObject item = content.getJSONObject(i);
                    AListFile file = new AListFile();
                    file.setName(item.getString("name"));
                    file.setPath(normalizePath(path + "/" + file.getName()));
                    file.setIsFolder(item.getBoolean("is_dir"));
                    file.setSize(item.optLong("size", 0));
                    file.setModified(item.optString("modified", ""));
                    if (!file.isFolder()) {
                        file.setUrl(getFileUrl(file.getPath()));
                    }
                    files.add(file);
                }

                App.post(() -> callback.onSuccess(files));

            } catch (Exception e) {
                e.printStackTrace();
                App.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void listFolders(String path, ListCallback callback) {
        Task.execute(() -> {
            try {
                String url = serverUrl + "api/fs/list";
                JSONObject body = new JSONObject();
                body.put("path", path);
                body.put("password", "");
                body.put("page", 1);
                body.put("per_page", 100);
                body.put("refresh", false);

                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json"),
                        body.toString()
                );

                Request.Builder builder = new Request.Builder()
                        .url(url)
                        .post(requestBody);

                if (!TextUtils.isEmpty(token)) {
                    builder.addHeader("Authorization", getAuthHeader());
                }

                Response response = OkHttp.client().newCall(builder.build()).execute();
                String result = response.body().string();

                JSONObject json = new JSONObject(result);
                int code = json.getInt("code");
                if (code != 200) {
                    String msg = json.optString("message", "Unknown error");
                    App.post(() -> callback.onError(msg));
                    return;
                }

                JSONObject data = json.getJSONObject("data");
                JSONArray content = data.getJSONArray("content");

                List<AListFile> files = new ArrayList<>();
                for (int i = 0; i < content.length(); i++) {
                    JSONObject item = content.getJSONObject(i);
                    boolean isDir = item.getBoolean("is_dir");
                    if (isDir) {
                        AListFile file = new AListFile();
                        file.setName(item.getString("name"));
                        file.setPath(normalizePath(path + "/" + file.getName()));
                        file.setIsFolder(true);
                        files.add(file);
                    }
                }

                App.post(() -> callback.onSuccess(files));

            } catch (Exception e) {
                e.printStackTrace();
                App.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private String normalizePath(String path) {
        if (TextUtils.isEmpty(path)) return "/";
        String normalized = path.replaceAll("/+", "/");
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    public String getFileUrl(String path) {
        String filePath = normalizePath(path);
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        String baseUrl = serverUrl + "d/" + filePath;
        if (!TextUtils.isEmpty(token)) {
            baseUrl += "?sign=" + token;
        }
        return baseUrl;
    }

    public interface FsGetCallback {
        void onSuccess(String rawUrl);
        void onError(String error);
    }

    public void getFsUrl(String path, FsGetCallback callback) {
        Task.execute(() -> {
            try {
                String url = serverUrl + "api/fs/get";
                JSONObject body = new JSONObject();
                body.put("path", normalizePath(path));
                body.put("password", "");

                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json"),
                        body.toString()
                );

                Request.Builder builder = new Request.Builder()
                        .url(url)
                        .post(requestBody);

                if (!TextUtils.isEmpty(token)) {
                    builder.addHeader("Authorization", getAuthHeader());
                }

                Response response = OkHttp.client().newCall(builder.build()).execute();
                String result = response.body().string();

                JSONObject json = new JSONObject(result);
                int code = json.getInt("code");
                if (code != 200) {
                    String msg = json.optString("message", "Unknown error");
                    App.post(() -> callback.onError(msg));
                    return;
                }

                JSONObject data = json.getJSONObject("data");
                String rawUrl = data.optString("raw_url", "");
                if (TextUtils.isEmpty(rawUrl)) {
                    rawUrl = getFileUrl(path);
                }
                final String finalUrl = rawUrl;
                App.post(() -> callback.onSuccess(finalUrl));

            } catch (Exception e) {
                e.printStackTrace();
                App.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public static class AListFile {
        private String name;
        private String path;
        private boolean isFolder;
        private long size;
        private String modified;
        private String url;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public boolean isFolder() { return isFolder; }
        public void setIsFolder(boolean isFolder) { this.isFolder = isFolder; }

        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }

        public String getModified() { return modified; }
        public void setModified(String modified) { this.modified = modified; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getExtension() {
            if (isFolder || TextUtils.isEmpty(name)) return "";
            int dotIndex = name.lastIndexOf('.');
            return dotIndex > 0 ? name.substring(dotIndex + 1).toLowerCase() : "";
        }

        public boolean isVideo() {
            String ext = getExtension();
            return ext.equals("mp4") || ext.equals("mkv") || ext.equals("avi") ||
                   ext.equals("mov") || ext.equals("wmv") || ext.equals("flv") ||
                   ext.equals("webm") || ext.equals("m4v") || ext.equals("ts") ||
                   ext.equals("m2ts") || ext.equals("mpg") || ext.equals("mpeg");
        }

        public boolean isImage() {
            String ext = getExtension();
            return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") ||
                   ext.equals("gif") || ext.equals("bmp") || ext.equals("webp") ||
                   ext.equals("svg") || ext.equals("tiff");
        }

        public boolean isAudio() {
            String ext = getExtension();
            return ext.equals("mp3") || ext.equals("flac") || ext.equals("wav") ||
                   ext.equals("aac") || ext.equals("m4a") || ext.equals("ogg") ||
                   ext.equals("ape") || ext.equals("wma");
        }

        public boolean isMedia() {
            return isVideo() || isImage();
        }

        public String getSizeStr() {
            if (isFolder) return "";
            if (size <= 0) return "0 B";
            final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
        }
    }
}
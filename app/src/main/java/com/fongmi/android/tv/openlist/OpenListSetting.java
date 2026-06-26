package com.fongmi.android.tv.openlist;

import com.fongmi.android.tv.event.ConfigEvent;
import com.github.catvod.utils.Prefers;

public class OpenListSetting {

    private static final String KEY_SERVER_URL = "openlist_server_url";
    private static final String KEY_TOKEN = "openlist_token";
    private static final String KEY_MOUNT_PATH = "openlist_mount_path";
    private static final String KEY_ENABLED = "openlist_enabled";

    public static boolean isEnabled() {
        return Prefers.getBoolean(KEY_ENABLED, false);
    }

    public static void putEnabled(boolean enabled) {
        Prefers.put(KEY_ENABLED, enabled);
        ConfigEvent.common();
    }

    public static String getServerUrl() {
        return Prefers.getString(KEY_SERVER_URL, "");
    }

    public static void putServerUrl(String url) {
        Prefers.put(KEY_SERVER_URL, url);
        ConfigEvent.common();
    }

    public static String getToken() {
        return Prefers.getString(KEY_TOKEN, "");
    }

    public static void putToken(String token) {
        Prefers.put(KEY_TOKEN, token);
    }

    public static String getMountPath() {
        return Prefers.getString(KEY_MOUNT_PATH, "/");
    }

    public static void putMountPath(String path) {
        Prefers.put(KEY_MOUNT_PATH, path);
    }

    public static AListApi createApi() {
        return new AListApi(getServerUrl(), getToken());
    }

    public static boolean hasConfig() {
        String url = getServerUrl();
        return !url.isEmpty();
    }
}
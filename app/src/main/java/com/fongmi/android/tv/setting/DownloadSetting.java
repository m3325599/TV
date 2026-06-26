package com.fongmi.android.tv.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.documentfile.provider.DocumentFile;

import com.fongmi.android.tv.App;
import com.github.catvod.utils.Prefers;

import java.io.File;

public class DownloadSetting {

    private static final String KEY_DOWNLOAD_PATH = "download_path";
    private static final String KEY_DOWNLOAD_ENABLED = "download_enabled";
    private static final String KEY_DOWNLOAD_QUALITY = "download_quality";

    public static boolean isEnabled() {
        return Prefers.getBoolean(KEY_DOWNLOAD_ENABLED, true);
    }

    public static void putEnabled(boolean enabled) {
        Prefers.put(KEY_DOWNLOAD_ENABLED, enabled);
    }

    public static String getPath() {
        return Prefers.getString(KEY_DOWNLOAD_PATH, getDefaultPath());
    }

    public static void putPath(String path) {
        Prefers.put(KEY_DOWNLOAD_PATH, path);
    }

    public static String getDefaultPath() {
        File dir = App.get().getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (dir != null) {
            return dir.getAbsolutePath() + "/TVBox";
        }
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/TVBox";
    }

    public static String getQuality() {
        return Prefers.getString(KEY_DOWNLOAD_QUALITY, "best");
    }

    public static void putQuality(String quality) {
        Prefers.put(KEY_DOWNLOAD_QUALITY, quality);
    }

    public static File getDownloadDir() {
        String path = getPath();
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static long getAvailableSpace() {
        File dir = getDownloadDir();
        if (dir.exists()) {
            android.os.StatFs stat = new android.os.StatFs(dir.getAbsolutePath());
            return stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        }
        return 0;
    }

    public static String formatSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        digitGroups = Math.min(digitGroups, units.length - 1);
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}

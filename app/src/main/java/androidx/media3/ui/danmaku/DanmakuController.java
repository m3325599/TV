package androidx.media3.ui.danmaku;

import android.net.Uri;

import androidx.annotation.Nullable;

import okhttp3.OkHttpClient;

public class DanmakuController {

    private DanmakuConfig config;
    private boolean enabled;

    public DanmakuController() {
        this.config = DanmakuConfig.DEFAULT;
        this.enabled = true;
    }

    public void setOkHttpClient(OkHttpClient client) {
    }

    public void setConfig(DanmakuConfig config) {
        this.config = config;
    }

    public DanmakuConfig getConfig() {
        return config;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void sendNow(String text) {
    }

    public void clearItems() {
    }

    public void setDataSource(@Nullable Uri uri) {
    }

    public void release() {
    }
}

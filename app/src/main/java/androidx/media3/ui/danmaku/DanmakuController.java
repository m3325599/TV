package androidx.media3.ui.danmaku;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import okhttp3.OkHttpClient;

public class DanmakuController extends View {

  public DanmakuController(Context context) {
    super(context);
  }

  public DanmakuController(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public DanmakuController(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void setOkHttpClient(OkHttpClient client) {
  }

  public void setConfig(DanmakuConfig config) {
  }

  public void setEnabled(boolean enabled) {
  }

  public void sendNow(String text) {
  }

  public void clearItems() {
  }

  public void setDataSource(Uri uri) {
  }

  public void release() {
  }
}

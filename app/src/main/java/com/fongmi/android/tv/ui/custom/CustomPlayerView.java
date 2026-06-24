package com.fongmi.android.tv.ui.custom;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.danmaku.DanmakuController;

public class CustomPlayerView extends PlayerView {

  private DanmakuController danmakuController;

  public CustomPlayerView(Context context) {
    super(context);
    init();
  }

  public CustomPlayerView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public CustomPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    danmakuController = new DanmakuController(getContext());
  }

  public DanmakuController getDanmakuController() {
    return danmakuController;
  }
}

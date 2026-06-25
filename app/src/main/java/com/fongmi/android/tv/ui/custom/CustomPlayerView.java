package com.fongmi.android.tv.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.media3.common.Player;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.danmaku.DanmakuController;

public class CustomPlayerView extends PlayerView {

  private DanmakuController danmakuController;
  private int render;

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
    setUseController(false);
  }

  public DanmakuController getDanmakuController() {
    return danmakuController;
  }

  public void setRender(int render) {
    this.render = render;
    Player player = getPlayer();
    if (player == null) return;
    setPlayer(null);
    View surfaceView = getVideoSurfaceView();
    View textureView = getVideoTextureView();
    if (surfaceView != null) surfaceView.setVisibility(render == 0 ? VISIBLE : GONE);
    if (textureView != null) textureView.setVisibility(render == 1 ? VISIBLE : GONE);
    setPlayer(player);
  }

  public int getRender() {
    return render;
  }
}

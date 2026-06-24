package androidx.media3.ui.danmaku;

import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class DanmakuConfig {

  public static final int STYLE_NONE = 0;
  public static final int STYLE_SHADOW = 1;
  public static final int STYLE_STROKE = 2;
  public static final int STYLE_PROJECTION = 3;

  public static final int COLOR_MODE_DEFAULT = 0;
  public static final int COLOR_MODE_COLORFUL = 1;
  public static final int COLOR_MODE_GRADIENT = 2;

  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({STYLE_NONE, STYLE_SHADOW, STYLE_STROKE, STYLE_PROJECTION})
  public @interface StyleMode {}

  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({COLOR_MODE_DEFAULT, COLOR_MODE_COLORFUL, COLOR_MODE_GRADIENT})
  public @interface ColorMode {}

  public static final DanmakuConfig DEFAULT = new Builder().build();

  public final float textScale;
  public final float transparency;
  public final boolean textBold;
  @StyleMode public final int styleMode;
  public final float shadowTransparency;
  public final float strokeWidthMultiplier;
  public final float projectionOffsetXMultiplier;
  public final float projectionOffsetYMultiplier;
  public final float projectionTransparency;
  @ColorMode public final int colorMode;

  public final long durationMs;
  public final long fixedDurationMs;
  public final long timeOffsetMs;

  public final int maxOnScreen;
  public final float scrollAreaRatio;
  public final float scrollGapRatio;
  public final float lineSpacing;
  public final int maxScrollLines;
  public final int maxTopLines;
  public final int maxBottomLines;

  public final boolean showScroll;
  public final boolean showTop;
  public final boolean showBottom;
  public final boolean showReverse;
  public final boolean showPositioned;
  public final boolean showSubtitle;
  public final boolean showSpecial;

  private DanmakuConfig(Builder builder) {
    this.textScale = builder.textScale;
    this.transparency = builder.transparency;
    this.textBold = builder.textBold;
    this.styleMode = builder.styleMode;
    this.shadowTransparency = builder.shadowTransparency;
    this.strokeWidthMultiplier = builder.strokeWidthMultiplier;
    this.projectionOffsetXMultiplier = builder.projectionOffsetXMultiplier;
    this.projectionOffsetYMultiplier = builder.projectionOffsetYMultiplier;
    this.projectionTransparency = builder.projectionTransparency;
    this.colorMode = builder.colorMode;
    this.durationMs = builder.durationMs;
    this.fixedDurationMs = builder.fixedDurationMs;
    this.timeOffsetMs = builder.timeOffsetMs;
    this.maxOnScreen = builder.maxOnScreen;
    this.scrollAreaRatio = builder.scrollAreaRatio;
    this.scrollGapRatio = builder.scrollGapRatio;
    this.lineSpacing = builder.lineSpacing;
    this.maxScrollLines = builder.maxScrollLines;
    this.maxTopLines = builder.maxTopLines;
    this.maxBottomLines = builder.maxBottomLines;
    this.showScroll = builder.showScroll;
    this.showTop = builder.showTop;
    this.showBottom = builder.showBottom;
    this.showReverse = builder.showReverse;
    this.showPositioned = builder.showPositioned;
    this.showSubtitle = builder.showSubtitle;
    this.showSpecial = builder.showSpecial;
  }

  public Builder buildUpon() {
    return new Builder()
        .setTextScale(textScale)
        .setTransparency(transparency)
        .setTextBold(textBold)
        .setStyleMode(styleMode)
        .setShadowTransparency(shadowTransparency)
        .setStrokeWidthMultiplier(strokeWidthMultiplier)
        .setProjectionOffsetXMultiplier(projectionOffsetXMultiplier)
        .setProjectionOffsetYMultiplier(projectionOffsetYMultiplier)
        .setProjectionTransparency(projectionTransparency)
        .setColorMode(colorMode)
        .setDurationMs(durationMs)
        .setFixedDurationMs(fixedDurationMs)
        .setTimeOffsetMs(timeOffsetMs)
        .setMaxOnScreen(maxOnScreen)
        .setScrollAreaRatio(scrollAreaRatio)
        .setScrollGapRatio(scrollGapRatio)
        .setLineSpacing(lineSpacing)
        .setMaxScrollLines(maxScrollLines)
        .setMaxTopLines(maxTopLines)
        .setMaxBottomLines(maxBottomLines)
        .setShowScroll(showScroll)
        .setShowTop(showTop)
        .setShowBottom(showBottom)
        .setShowReverse(showReverse)
        .setShowPositioned(showPositioned)
        .setShowSubtitle(showSubtitle)
        .setShowSpecial(showSpecial);
  }

  public static final class Builder {

    private float textScale = 1.0f;
    private float transparency = 0.0f;
    private boolean textBold = false;
    @StyleMode private int styleMode = STYLE_STROKE;
    private float shadowTransparency = 0.1f;
    private float strokeWidthMultiplier = 0.12f;
    private float projectionOffsetXMultiplier = 0.08f;
    private float projectionOffsetYMultiplier = 0.08f;
    private float projectionTransparency = 0.2f;
    @ColorMode private int colorMode = COLOR_MODE_DEFAULT;

    private long durationMs = 8000L;
    private long fixedDurationMs = 5000L;
    private long timeOffsetMs = 0L;

    private int maxOnScreen = 150;
    private float scrollAreaRatio = 0.5f;
    private float scrollGapRatio = 0.0f;
    private float lineSpacing = 1.4f;
    private int maxScrollLines = 0;
    private int maxTopLines = 0;
    private int maxBottomLines = 0;

    private boolean showScroll = true;
    private boolean showTop = true;
    private boolean showBottom = true;
    private boolean showReverse = true;
    private boolean showPositioned = true;
    private boolean showSubtitle = true;
    private boolean showSpecial = true;

    public Builder() {}

    public Builder setTextScale(@FloatRange(from = 0.1, to = 4.0) float textScale) {
      this.textScale = textScale;
      return this;
    }

    public Builder setTransparency(@FloatRange(from = 0.0, to = 1.0) float transparency) {
      this.transparency = transparency;
      return this;
    }

    public Builder setTextBold(boolean textBold) {
      this.textBold = textBold;
      return this;
    }

    public Builder setStyleMode(@StyleMode int styleMode) {
      this.styleMode = styleMode;
      return this;
    }

    public Builder setShadowTransparency(@FloatRange(from = 0.0, to = 1.0) float shadowTransparency) {
      this.shadowTransparency = shadowTransparency;
      return this;
    }

    public Builder setStrokeWidthMultiplier(@FloatRange(from = 0.0) float strokeWidthMultiplier) {
      this.strokeWidthMultiplier = strokeWidthMultiplier;
      return this;
    }

    public Builder setProjectionOffsetXMultiplier(@FloatRange(from = 0.0) float projectionOffsetXMultiplier) {
      this.projectionOffsetXMultiplier = projectionOffsetXMultiplier;
      return this;
    }

    public Builder setProjectionOffsetYMultiplier(@FloatRange(from = 0.0) float projectionOffsetYMultiplier) {
      this.projectionOffsetYMultiplier = projectionOffsetYMultiplier;
      return this;
    }

    public Builder setProjectionTransparency(@FloatRange(from = 0.0, to = 1.0) float projectionTransparency) {
      this.projectionTransparency = projectionTransparency;
      return this;
    }

    public Builder setColorMode(@ColorMode int colorMode) {
      this.colorMode = colorMode;
      return this;
    }

    public Builder setDurationMs(@IntRange(from = 1000) long durationMs) {
      this.durationMs = durationMs;
      return this;
    }

    public Builder setFixedDurationMs(@IntRange(from = 1000) long fixedDurationMs) {
      this.fixedDurationMs = fixedDurationMs;
      return this;
    }

    public Builder setTimeOffsetMs(long timeOffsetMs) {
      this.timeOffsetMs = timeOffsetMs;
      return this;
    }

    public Builder setMaxOnScreen(@IntRange(from = 1) int maxOnScreen) {
      this.maxOnScreen = maxOnScreen;
      return this;
    }

    public Builder setScrollAreaRatio(@FloatRange(from = 0.1, to = 1.0) float scrollAreaRatio) {
      this.scrollAreaRatio = scrollAreaRatio;
      return this;
    }

    public Builder setScrollGapRatio(@FloatRange(from = 0.0) float scrollGapRatio) {
      this.scrollGapRatio = scrollGapRatio;
      return this;
    }

    public Builder setLineSpacing(@FloatRange(from = 0.5, to = 5.0) float lineSpacing) {
      this.lineSpacing = lineSpacing;
      return this;
    }

    public Builder setMaxScrollLines(@IntRange(from = 0) int maxScrollLines) {
      this.maxScrollLines = maxScrollLines;
      return this;
    }

    public Builder setMaxTopLines(@IntRange(from = 0) int maxTopLines) {
      this.maxTopLines = maxTopLines;
      return this;
    }

    public Builder setMaxBottomLines(@IntRange(from = 0) int maxBottomLines) {
      this.maxBottomLines = maxBottomLines;
      return this;
    }

    public Builder setShowScroll(boolean showScroll) {
      this.showScroll = showScroll;
      return this;
    }

    public Builder setShowTop(boolean showTop) {
      this.showTop = showTop;
      return this;
    }

    public Builder setShowBottom(boolean showBottom) {
      this.showBottom = showBottom;
      return this;
    }

    public Builder setShowReverse(boolean showReverse) {
      this.showReverse = showReverse;
      return this;
    }

    public Builder setShowPositioned(boolean showPositioned) {
      this.showPositioned = showPositioned;
      return this;
    }

    public Builder setShowSubtitle(boolean showSubtitle) {
      this.showSubtitle = showSubtitle;
      return this;
    }

    public Builder setShowSpecial(boolean showSpecial) {
      this.showSpecial = showSpecial;
      return this;
    }

    public DanmakuConfig build() {
      return new DanmakuConfig(this);
    }
  }
}

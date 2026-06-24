package androidx.media3.common;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public final class MediaTitle implements Parcelable {

  public static final Creator<MediaTitle> CREATOR =
      new Creator<MediaTitle>() {
        @Override
        public MediaTitle createFromParcel(Parcel in) {
          return new MediaTitle(in);
        }

        @Override
        public MediaTitle[] newArray(int size) {
          return new MediaTitle[size];
        }
      };

  public final int index;
  @Nullable public final String title;
  @Nullable public final String label;
  public final long durationUs;
  public boolean selected;

  public MediaTitle(int index, @Nullable String title, @Nullable String label, long durationUs) {
    this.index = index;
    this.title = title;
    this.label = label;
    this.durationUs = durationUs;
    this.selected = false;
  }

  protected MediaTitle(Parcel in) {
    index = in.readInt();
    title = in.readString();
    label = in.readString();
    durationUs = in.readLong();
    selected = in.readByte() != 0;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(index);
    dest.writeString(title);
    dest.writeString(label);
    dest.writeLong(durationUs);
    dest.writeByte((byte) (selected ? 1 : 0));
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MediaTitle)) {
      return false;
    }
    MediaTitle other = (MediaTitle) obj;
    return index == other.index
        && durationUs == other.durationUs
        && selected == other.selected
        && (title == null ? other.title == null : title.equals(other.title))
        && (label == null ? other.label == null : label.equals(other.label));
  }

  @Override
  public int hashCode() {
    int result = index;
    result = 31 * result + (title == null ? 0 : title.hashCode());
    result = 31 * result + (label == null ? 0 : label.hashCode());
    result = 31 * result + (int) (durationUs ^ (durationUs >>> 32));
    result = 31 * result + (selected ? 1 : 0);
    return result;
  }

  public static final class Builder {
    private int index;
    @Nullable private String title;
    @Nullable private String label;
    private long durationUs;

    public Builder() {}

    public Builder setIndex(int index) {
      this.index = index;
      return this;
    }

    public Builder setTitle(@Nullable String title) {
      this.title = title;
      return this;
    }

    public Builder setLabel(@Nullable String label) {
      this.label = label;
      return this;
    }

    public Builder setDurationUs(long durationUs) {
      this.durationUs = durationUs;
      return this;
    }

    public MediaTitle build() {
      return new MediaTitle(index, title, label, durationUs);
    }
  }
}

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

  public MediaTitle(int index, @Nullable String title) {
    this.index = index;
    this.title = title;
  }

  protected MediaTitle(Parcel in) {
    index = in.readInt();
    title = in.readString();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(index);
    dest.writeString(title);
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
        && (title == null ? other.title == null : title.equals(other.title));
  }

  @Override
  public int hashCode() {
    int result = index;
    result = 31 * result + (title == null ? 0 : title.hashCode());
    return result;
  }
}

package androidx.media3.common;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public final class MediaTitle implements Parcelable {

    public static final Creator<MediaTitle> CREATOR = new Creator<MediaTitle>() {
        @Override
        public MediaTitle createFromParcel(Parcel in) {
            return new MediaTitle(in);
        }

        @Override
        public MediaTitle[] newArray(int size) {
            return new MediaTitle[size];
        }
    };

    private final CharSequence title;
    private final int id;

    public MediaTitle(CharSequence title, int id) {
        this.title = title;
        this.id = id;
    }

    protected MediaTitle(Parcel in) {
        this.title = in.readCharSequence();
        this.id = in.readInt();
    }

    public CharSequence getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeCharSequence(title);
        dest.writeInt(id);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MediaTitle)) return false;
        MediaTitle other = (MediaTitle) obj;
        return id == other.id && (title == null ? other.title == null : title.equals(other.title));
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + id;
        return result;
    }
}

package ironbear775.com.musicplayer.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by ironbear on 2016/12/9.
 */

//使用Parcelable来规格化数据，从而通过intent传递数据
public class Music implements Parcelable, Serializable {
    private long ID; //歌曲ID
    private long size; //歌曲文件大小
    private int duration; //歌曲时长
    private String album_id; //专辑封面id
    private String title; //歌曲标题名称
    private String album; //歌曲专辑名称
    private String artist; //歌曲艺术家名称
    private String uri; //歌曲对应Android媒体库的uri
    private String albumArtUri; //歌曲专辑封面对应Android媒体库的uri
    private String track; //歌曲专辑音轨

    public Music() {

    }

    private Music(Parcel in) {
        ID = in.readLong();
        size = in.readLong();
        duration = in.readInt();
        album_id = in.readString();
        title = in.readString();
        album = in.readString();
        artist = in.readString();
        uri = in.readString();
        albumArtUri = in.readString();
        track = in.readString();
    }

    public long getID() {
        return ID;
    }

    public void setID(long id) {
        this.ID = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(String id) {
        this.album_id = id;
    }

    public String getAlbumArtUri() {
        return albumArtUri;
    }

    public void setAlbumArtUri(String albumArtUri) {
        this.albumArtUri = albumArtUri;
    }
    public String getTrack() {
        return track;
    }

    public void setTrack(String track1) {
        this.track = track1;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(ID);
        dest.writeLong(size);
        dest.writeInt(duration);
        dest.writeString(album_id);
        dest.writeString(title);
        dest.writeString(album);
        dest.writeString(artist);
        dest.writeString(uri);
        dest.writeString(albumArtUri);
        dest.writeString(track);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public Music createFromParcel(Parcel source) {
            return new Music(source);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}

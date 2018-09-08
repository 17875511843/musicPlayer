package ironbear775.com.musicplayer.entity;

/**
 * Created by ironbear on 2017/2/3.
 */

public class Playlist {
    private String name; //自定义播放列表名称
    private String count; //列表歌曲个数

    public Playlist(String name,String count){
        this.name = name;
        this.count = count;
    }

    public void setName(String listName){
        this.name = listName;
    }

    public void setCount(String listCount){
        count = listCount;
    }

    public String getName() {
        return name;
    }

    public String getCount() {
        return count;
    }

}

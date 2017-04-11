package ironbear775.com.musicplayer.Class;

/**
 * Created by ironbear on 2017/2/3.
 */

public class Playlist {
    private final String name;
    private final String count;
    public Playlist(String name,String count){
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public String getCount() {
        return count;
    }

}

package ironbear775.com.musicplayer.Util;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ironbear on 2017/2/3.
 */

public class PlaylistDbHelper extends SQLiteOpenHelper {
    private final String create_db;

    public PlaylistDbHelper(Context context, String name, String mTable) {
        super(context, name, null, 1);
        create_db = mTable;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(create_db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}

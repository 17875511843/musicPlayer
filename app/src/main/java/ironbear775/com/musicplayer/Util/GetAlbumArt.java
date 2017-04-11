package ironbear775.com.musicplayer.Util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

import ironbear775.com.musicplayer.R;


/**
 * Created by ironbear on 2016/12/28.
 */

public class GetAlbumArt {
    public static Bitmap getAlbumArtBitmap(Context context, String albumArtUri, int option){
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = option;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        InputStream in;
        ContentResolver resolver = context.getContentResolver();
        if (albumArtUri != null){
            try {
                in = resolver.openInputStream(android.net.Uri.parse(albumArtUri));
                bitmap = BitmapFactory.decodeStream(in,null,options);
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album_art,options);
        }
        return bitmap;
    }
}

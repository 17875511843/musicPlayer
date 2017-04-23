package ironbear775.com.musicplayer.Util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.R;

/**
 * Created by ironbear on 2017/2/6.
 */

public class DetailDialog extends Dialog {
    private ArrayList<Music> list = new ArrayList<>();
    private final int pos;

    public DetailDialog(Context context, ArrayList<Music> musics, int position) {
        super(context);
        list = musics;
        pos = position;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_dialog_layout);
        TextView filename = (TextView) findViewById(R.id.filename);
        TextView filepath = (TextView) findViewById(R.id.filePath);
        TextView fileDuration = (TextView) findViewById(R.id.fileduration);
        TextView fileArtist = (TextView) findViewById(R.id.fileArtist);
        TextView fileAlbum = (TextView) findViewById(R.id.fileAlbum);
        TextView fileBitrate = (TextView) findViewById(R.id.bit);
        TextView fileSamplingRate = (TextView) findViewById(R.id.rate);

        SimpleDateFormat time = new SimpleDateFormat("m:ss");

        filepath.setText(list.get(pos).getUri());
        filename.setText(list.get(pos).getTitle());
        fileArtist.setText(list.get(pos).getArtist());
        fileAlbum.setText(list.get(pos).getAlbum());

        try {
            Mp3File file = new Mp3File(list.get(pos).getUri());
            String duration = time.format(file.getLengthInMilliseconds()) + "";
            fileDuration.setText(duration);

            fileSamplingRate.setText("" + file.getSampleRate() + " Hz");
            fileBitrate.setText("" + file.getBitrate() + "kb/s");

        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        }

    }
}

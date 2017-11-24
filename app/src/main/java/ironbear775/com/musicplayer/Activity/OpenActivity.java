package ironbear775.com.musicplayer.Activity;

import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MusicService;

/**
 * Created by ironbear on 2017/5/12.
 */

public class OpenActivity extends BaseActivity {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ImageView control;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_layout);
        TextView title = findViewById(R.id.open_title);
        TextView others = findViewById(R.id.open_others);
        control = findViewById(R.id.open_control);
        seekBar = findViewById(R.id.open_seekbar);
        ImageView albumArt = findViewById(R.id.open_image);

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {

            File file = new File(getIntent().getData().getPath());

            Cursor cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Audio.Media.DATA + "=?",
                    new String[]{getIntent().getData().getPath()},
                    MediaStore.Audio.Media.TITLE);
            if (cursor != null) {
                cursor.moveToFirst();
                title.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                String othersText = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                        + "-" + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                others.setText(othersText);
                Glide.with(this)
                        .load(String.valueOf(ContentUris.withAppendedId(
                                Uri.parse("content://media/external/audio/albumart")
                                , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))))
                        .placeholder(R.drawable.default_album_art)
                        .into(albumArt);
                cursor.close();
            }


            int flag = 0;
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

            if (am != null) {
                List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(100);
                for (ActivityManager.RunningServiceInfo info : services) {
                    // 得到所有正在运行的服务的名称
                    String name = info.service.getClassName();
                    if (name.equals("ironbear775.com.musicplayer.Service.MusicService")) {
                        flag = 1;
                        break;
                    }
                }
                try {
                    mediaPlayer.setDataSource(file.getPath());
                    mediaPlayer.prepare();
                    if (flag == 1) {
                        MusicService.mediaPlayer.pause();
                        Intent intent = new Intent("pause music");
                        sendBroadcast(intent);
                    }
                    mediaPlayer.start();
                    control.setImageResource(R.drawable.footpause);

                    control.setOnClickListener(v -> {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            control.setImageResource(R.drawable.footplay);
                        } else {
                            mediaPlayer.start();
                            control.setImageResource(R.drawable.footpause);
                        }
                    });
                    handler.post(runnable);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

    }



    public static final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            });
            handler.postDelayed(runnable, 1000);
        }
    };


    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }
}

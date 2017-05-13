package ironbear775.com.musicplayer.Activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MusicService;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.Notification;

/**
 * Created by ironbear on 2017/5/12.
 */

public class OpenActivity extends BaseActivity {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ImageView control;
    private SeekBar seekBar;
    private Notification notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_layout);
        TextView title = (TextView) findViewById(R.id.open_title);
        TextView others = (TextView) findViewById(R.id.open_others);
        control = (ImageView) findViewById(R.id.open_control);
        seekBar = (SeekBar) findViewById(R.id.open_seekbar);

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {

            File file = new File(getIntent().getData().getPath());

            Cursor cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Audio.Media.DATA + "=?",
                    new String[]{getIntent().getData().getPath()},
                    MediaStore.Audio.Media.TITLE);
            cursor.moveToFirst();

            title.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            others.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    + "-" + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));

            int flag = 0;
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
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
                if (flag == 1){
                    MusicService.mediaPlayer.pause();

                    notification = new Notification(MusicService.musicService);

                    createNewNotification(R.drawable.footplay);

                }
                mediaPlayer.start();
                control.setImageResource(R.drawable.footpause);

                control.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            control.setImageResource(R.drawable.footplay);
                        } else {
                            mediaPlayer.start();
                            control.setImageResource(R.drawable.footpause);
                        }
                    }
                });
                handler.post(runnable);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    private void createNewNotification(final int id) {
        final Message msg = new Message();

        if (MusicService.music.getAlbumArtUri() != null) {
            Glide.with(getApplicationContext())
                    .load(MusicService.music.getAlbumArtUri())
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {

                                    Palette.Swatch swatch = palette.getVibrantSwatch();
                                    if (swatch != null) {
                                        msg.what = swatch.getRgb();
                                        msg.arg1 = swatch.getTitleTextColor();
                                        msg.arg2 = swatch.getBodyTextColor();
                                        msg.obj = MusicUtils.messageGood;

                                        notification.createNotification(getBaseContext(), id,
                                                MusicService.musicList, msg);
                                    } else {
                                        swatch = palette.getMutedSwatch();
                                        if (swatch != null) {
                                            msg.what = swatch.getRgb();
                                            msg.arg1 = swatch.getTitleTextColor();
                                            msg.arg2 = swatch.getBodyTextColor();
                                            msg.obj = MusicUtils.messageGood;

                                            notification.createNotification(getBaseContext(), id,
                                                    MusicService.musicList, msg);
                                        } else {
                                            msg.obj = MusicUtils.messageBad;
                                            notification.createNotification(getBaseContext(), id,
                                                    MusicService.musicList, msg);
                                        }
                                    }
                                }
                            });
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            msg.obj = MusicUtils.messageNull;
                            notification.createNotification(getBaseContext(), id,
                                    MusicService.musicList, msg);
                        }
                    });
        } else {
            msg.obj = MusicUtils.messageNull;
            notification.createNotification(getBaseContext(), id,
                    MusicService.musicList, msg);
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

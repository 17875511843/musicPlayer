package ironbear775.com.musicplayer.Service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Fragment.MusicListFragment;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.Notification;

/**
 * Created by ironbear on 2016/12/14.
 */

public class MusicService extends Service {

    public static ArrayList<Music> musicList = new ArrayList<>();
    public static int musicPosition;
    public static Music music;
    public static final MediaPlayer mediaPlayer = new MediaPlayer();
    public static boolean isRandom = false;
    public static int isSingleOrCycle = 1;//1为cycle 2single 3 none
    public static MusicService musicService;

    private Notification notification;
    private AudioManager audioManager;
    private boolean haveFocus;
    private int[] last;
    private int i = 0;
    private boolean isPlug = false;

    //回传数据,返回Binder类
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    //获取ListView传来的音乐列表和当前列表位置
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        registerReceiver(receiver, intentFilter);

        musicService = this;

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        notification = new Notification(this);
        switch (intent.getAction()) {
            case "musiclist":
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    //Bundle bundle = intent.getBundleExtra("value");
                    musicList = intent.getParcelableArrayListExtra("musicList");
                    //bundle.getParcelableArrayList("musicList");
                    musicPosition = intent.getIntExtra("musicPosition", 0);
                    //bundle.getInt("musicPosition");
                    int progress = intent.getIntExtra("musicProgress", 0);
                    //bundle.getInt("musicProgress");
                    last = new int[musicList.size()];
                    i = 0;
                    initMusic(musicPosition, progress);
                    MusicUtils.saveInfoService(getApplicationContext());
                    playOrPause();

                }
                break;
            case "PreMusic":
                preMusic();
                break;
            case "isPause":
                playOrPause();
                notification.setNotificationPlayOrPause(getBaseContext(), R.drawable.footplay);
                break;
            case "isPlaying":
                playOrPause();
                break;
            case "NextMusic":
                Glide.with(getBaseContext())
                        .load(R.drawable.default_album_art)
                        .thumbnail(0.5f)
                        .into(MusicList.footAlbumArt);
                nextMusic();
                break;
            case "ClearMusic":
                MusicList.PlayOrPause.setImageResource(R.drawable.footplaywhite);
                mediaPlayer.pause();
                MusicList.flag = 0;

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(1);
                stopForeground(true);
                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();

                String musicUri = music.getUri();
                for (int i = 0; i < MusicListFragment.musicList.size(); i++) {
                    if (MusicListFragment.musicList.get(i).getUri().equals(musicUri)) {
                        editor.putInt("position", i);
                        break;
                    }
                }
                editor.putInt("progress", mediaPlayer.getCurrentPosition());
                editor.putBoolean("isRandom", isRandom);
                editor.putInt("isSingleOrCycle", isSingleOrCycle);
                editor.putInt("flag", 0);
                editor.apply();
                editor.commit();
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (!mediaPlayer.isPlaying() && haveFocus) {
                        haveFocus = false;
                        mediaPlayer.start();
                        notification.setNotificationPlayOrPause(getBaseContext(), R.drawable.footpause);
                        MusicList.PlayOrPause.setImageResource(R.drawable.footpausewhite);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    haveFocus = false;
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        createNewNotification(R.drawable.footplay);

                        MusicList.PlayOrPause.setImageResource(R.drawable.footplaywhite);
                    }
                    audioManager.abandonAudioFocus(audioFocusChangeListener);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (mediaPlayer.isPlaying()) {
                        haveFocus = true;
                        mediaPlayer.pause();
                        createNewNotification(R.drawable.footplay);
                        MusicList.PlayOrPause.setImageResource(R.drawable.footplaywhite);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };

    //初始化音乐，准备播放
    private void initMusic(int musicPosition, int progress) {
        if (musicList.size() >= 1) {
            music = musicList.get(musicPosition);
            String uri = music.getUri();
            try {
                File file = new File(uri);
                mediaPlayer.reset();
                mediaPlayer.setDataSource(file.getPath());
                mediaPlayer.prepare();
                mediaPlayer.seekTo(progress);
                if (isRandom) {
                    last[i] = musicPosition;
                }
            } catch (Exception e) {
                Log.d("InitMusic", "init failed");
                e.printStackTrace();
            }
        }
    }

    //播放或暂停
    public void playOrPause() {
        audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            MusicList.PlayOrPause.setImageResource(R.drawable.footplaywhite);
            createNewNotification(R.drawable.footplay);
            haveFocus = false;
        } else {
            mediaPlayer.start();
            createNewNotification(R.drawable.footpause);
            MusicList.PlayOrPause.setImageResource(R.drawable.footpausewhite);
            haveFocus = true;
        }
        //判断播放模式
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (isSingleOrCycle == 2) {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                    createNewNotification(R.drawable.footpause);
                } else if (isSingleOrCycle == 3) {
                    mediaPlayer.pause();
                    createNewNotification(R.drawable.footplay);
                } else if (isSingleOrCycle == 1) {
                    nextMusic();
                    createNewNotification(R.drawable.footpause);
                }
            }
        });
        MusicUtils.saveInfoService(getApplicationContext());
        if (MusicService.mediaPlayer.isPlaying()) {
            try {
                Mp3File file = new Mp3File(MusicService.music.getUri());
                if (file.hasId3v2Tag()) {
                    if (file.getId3v2Tag().getLyrics() != null) {
                        MusicList.lyricButton.setVisibility(View.VISIBLE);
                    }else {
                        MusicList.lyricButton.setVisibility(View.GONE);
                        MusicList.lyricView.setVisibility(View.GONE);
                    }
                }

            } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                e.printStackTrace();
            }
        }
    }

    public void nextMusic() {
        audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        mediaPlayer.stop();
        try {
            mediaPlayer.reset();
            if (isRandom) {
                if (musicList.size() == 1) {
                    musicPosition = 0;
                } else {
                    musicPosition = createRandom();
                    i++;
                    if (i >= musicList.size()) {
                        i = 0;
                    }
                    last[i] = musicPosition;
                }
            } else if (isSingleOrCycle == 1 || isSingleOrCycle == 3) {
                if (musicList.size() == 1) {
                    musicPosition = 0;
                } else if (musicPosition == musicList.size() - 1) {
                    musicPosition = 0;
                } else
                    musicPosition++;
            } else if (isSingleOrCycle == 2) {
                if (musicPosition == musicList.size() - 1) {
                    musicPosition = 0;
                } else
                    musicPosition++;
            }
            initMusic(musicPosition, 0);
            mediaPlayer.start();

            MusicList.footArtist.setText(musicList.get(musicPosition).getArtist());
            MusicList.footTitle.setText(musicList.get(musicPosition).getTitle());

            Glide.with(this)
                    .load(music.getAlbumArtUri())
                    .centerCrop()
                    .placeholder(R.drawable.default_album_art)
                    .into(MusicList.accountHeader.getHeaderBackgroundView());
            Glide.with(this)
                    .load(music.getAlbumArtUri())
                    .thumbnail(0.5f)
                    .placeholder(R.drawable.default_album_art)
                    .into(MusicList.footAlbumArt);

            MusicList.PlayOrPause.setImageResource(R.drawable.footpausewhite);
            Intent intent = new Intent("Music play to the end");
            sendBroadcast(intent);
            createNewNotification(R.drawable.footpause);

            MusicUtils.saveInfoService(getApplicationContext());
        } catch (Exception e) {
            Log.d("hint", "can't jump next music");
            e.printStackTrace();
        }
        Intent intent = new Intent("sendPosition");
        intent.putExtra("position", musicPosition);
        sendBroadcast(intent);
        if (MusicService.mediaPlayer.isPlaying()) {
            try {
                Mp3File file = new Mp3File(MusicService.music.getUri());
                if (file.hasId3v2Tag()) {
                    if (file.getId3v2Tag().getLyrics() != null) {
                        MusicList.lyricButton.setVisibility(View.VISIBLE);
                    }else {
                        MusicList.lyricButton.setVisibility(View.GONE);

                    }
                }
                if (MusicList.lyricView.getVisibility() == View.VISIBLE) {
                    MusicList.lyricView.setVisibility(View.GONE);
                }
            } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                e.printStackTrace();
            }
        }
    }

    public void preMusic() {
        audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        mediaPlayer.stop();
        try {
            mediaPlayer.reset();
            if (isRandom) {
                if (i > 1) {
                    i--;
                    musicPosition = last[i];
                } else
                    musicPosition = last[i];
            } else if (isSingleOrCycle == 1 || isSingleOrCycle == 3) {
                if (musicPosition == 0) {
                    musicPosition = musicList.size() - 1;
                } else
                    musicPosition--;
            } else if (isSingleOrCycle == 2) {
                if (musicPosition == 0) {
                    musicPosition = musicList.size() - 1;
                } else
                    musicPosition--;
            }
            initMusic(musicPosition, 0);
            mediaPlayer.start();

            MusicList.footArtist.setText(musicList.get(musicPosition).getArtist());
            MusicList.footTitle.setText(musicList.get(musicPosition).getTitle());

            Glide.with(this)
                    .load(music.getAlbumArtUri())
                    .centerCrop()
                    .placeholder(R.drawable.default_album_art)
                    .into(MusicList.accountHeader.getHeaderBackgroundView());
            Glide.with(this)
                    .load(music.getAlbumArtUri())
                    .thumbnail(0.5f)
                    .placeholder(R.drawable.default_album_art)
                    .into(MusicList.footAlbumArt);

            MusicList.PlayOrPause.setImageResource(R.drawable.footpausewhite);
            Intent intent = new Intent("Music play to the end");
            sendBroadcast(intent);
            createNewNotification(R.drawable.footpause);
            MusicUtils.saveInfoService(getApplicationContext());
        } catch (Exception e) {
            Log.d("hint", "can't jump pre music");
            e.printStackTrace();
        }
        Intent intent = new Intent("sendPosition");
        intent.putExtra("position", musicPosition);
        sendBroadcast(intent);
        if (MusicService.mediaPlayer.isPlaying()) {
            try {
                Mp3File file = new Mp3File(MusicService.music.getUri());
                if (file.hasId3v2Tag()) {
                    if (file.getId3v2Tag().getLyrics() != null) {
                        MusicList.lyricButton.setVisibility(View.VISIBLE);
                    }else {
                        MusicList.lyricButton.setVisibility(View.GONE);

                    }
                }
                if (MusicList.lyricView.getVisibility() == View.VISIBLE) {
                    MusicList.lyricView.setVisibility(View.GONE);
                }
            } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                e.printStackTrace();
            }
        }
    }

    private int createRandom() {

        Random random = new Random();
        int randomInt;
        if (musicList.size() == 0) {
            randomInt = 0;
        } else {
            randomInt = random.nextInt(musicList.size());
        }
        return randomInt;
    }


    @Override
    public void onDestroy() {

        mediaPlayer.stop();
        mediaPlayer.release();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(1);

        MusicUtils.saveInfoService(getApplicationContext());
        audioManager.abandonAudioFocus(audioFocusChangeListener);
        unregisterReceiver(receiver);
        super.onDestroy();
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

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    if (mediaPlayer.isPlaying() && isPlug) {
                        mediaPlayer.pause();
                        MusicList.PlayOrPause.setImageResource(R.drawable.footplaywhite);
                        createNewNotification(R.drawable.footplay);
                    }
                    break;
                case AudioManager.ACTION_HEADSET_PLUG:
                    if (intent.hasExtra("state")) {
                        isPlug = intent.getIntExtra("state", 0) != 0;
                    }
                    break;
            }
        }
    };
}

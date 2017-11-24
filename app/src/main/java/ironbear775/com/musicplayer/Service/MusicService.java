package ironbear775.com.musicplayer.Service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import ironbear775.com.musicplayer.Activity.Equalizer;
import ironbear775.com.musicplayer.Activity.MusicList;
import ironbear775.com.musicplayer.Activity.SearchActivity;
import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Fragment.AlbumDetailFragment;
import ironbear775.com.musicplayer.Fragment.ArtistDetailFragment;
import ironbear775.com.musicplayer.Fragment.FolderDetailFragment;
import ironbear775.com.musicplayer.Fragment.MusicListFragment;
import ironbear775.com.musicplayer.Fragment.MusicRecentAddedFragment;
import ironbear775.com.musicplayer.Fragment.PlaylistDetailFragment;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.GetAlbumArt;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.Notification;

/**
 * Created by ironbear on 2016/12/14.
 */

public class MusicService extends Service {

    public static ArrayList<Music> shuffleList = new ArrayList<>();
    public static ArrayList<Music> musicList = new ArrayList<>();
    public static ArrayList<Music> onPlayingList = new ArrayList<>();
    public static int musicPosition;
    public static Music music;
    public static final MediaPlayer mediaPlayer = new MediaPlayer();
    public static boolean isRandom = false;
    public static int isSingleOrCycle = 1;//1为cycle 2single 3 none
    public static MusicService musicService;

    private Notification notification;
    private AudioManager audioManager;
    private boolean haveFocus;
    private boolean isPlug = false;
    private android.media.audiofx.Equalizer equalizer;
    private MusicUtils musicUtils;
    private SharedPreferences.Editor editor;
    private MusicServiceReceiver musicServiceReceiver;
    private boolean canPlay = false;

    Intent in = new Intent(AudioEffect
            .ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);

    //回传数据,返回Binder类
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {

        musicServiceReceiver = new MusicServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        intentFilter.addAction("NEXT");
        intentFilter.addAction("PlAYORPAUSE");
        intentFilter.addAction("PREVIOUS");
        intentFilter.addAction("STOP");
        intentFilter.addAction("setPlayOrPause");
        intentFilter.addAction("enableColorNotification");
        intentFilter.addAction("refresh notification");
        intentFilter.addAction("setNotification");
        intentFilter.addAction("pause music");
        intentFilter.addAction("clear");
        intentFilter.addAction("cycle list");
        intentFilter.addAction("random play");
        intentFilter.addAction("cycle play");
        intentFilter.addAction("clear");
        intentFilter.addAction("play next");
        intentFilter.addAction("delete current music success");
        registerReceiver(musicServiceReceiver, intentFilter);
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    //获取ListView传来的音乐列表和当前列表位置
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        initView();

        int result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        notification = new Notification(this);
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case "musiclist":
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        int from = intent.getIntExtra("from", 1);
                        switch (from) {
                            case 1:
                                musicList = MusicListFragment.musicList;
                                break;
                            case 2:
                                musicList = ArtistDetailFragment.musicList;
                                break;
                            case 3:
                                musicList = AlbumDetailFragment.musicList;
                                break;
                            case 4:
                                musicList = PlaylistDetailFragment.musicList;
                                break;
                            case 5:
                                musicList = FolderDetailFragment.musicList;
                                break;
                            case 6:
                                musicList = MusicRecentAddedFragment.musicList;
                                break;
                            case 7:
                                musicList = SearchActivity.musicList;
                                break;
                            case 8:
                                musicList = MusicList.list;
                                break;
                            case 9:
                                shuffleList = MusicUtils.arrayList;
                        }
                        musicPosition = intent.getIntExtra("musicPosition", 0);
                        if (isRandom) {
                            if (from != 9 && from != 8) {
                                Log.d("TAG", "Title: " + musicList.get(musicPosition).getTitle());
                                shuffleList = musicUtils.createShuffleList(musicList);
                                int p = 0;
                                for (int i = 0; i < shuffleList.size(); i++) {
                                    if (shuffleList.get(i).getUri().equals(
                                            musicList.get(musicPosition).getUri())) {
                                        p = i;
                                        break;
                                    }
                                }
                                Music temp = shuffleList.get(0);
                                shuffleList.set(0, shuffleList.get(p));
                                shuffleList.set(p, temp);
                                for (int i = 0; i < shuffleList.size(); i++) {
                                    Log.d("shuffleList", "shuffleList: " + shuffleList.get(i).getTitle());
                                }
                            } else if (from == 8) {
                                shuffleList = MusicUtils.getShuffleArray(this);
                            }
                            if (shuffleList != null && shuffleList.size() > 0) {
                                onPlayingList = shuffleList;
                                if (from != 8)
                                    musicPosition = 0;
                            } else
                                onPlayingList = musicList;
                        } else {
                            if (from == 8)
                                onPlayingList = MusicUtils.getArray(this);
                            else
                                onPlayingList = musicList;
                        }

                        if (shuffleList == null || shuffleList.size() == 0)
                            MusicUtils.saveShuffleArray(this, musicList);
                        else
                            MusicUtils.saveShuffleArray(this, shuffleList);

                        MusicUtils.saveArray(this, musicList);

                        int progress = intent.getIntExtra("musicProgress", 0);

                        initMusic(musicPosition, progress);
                        playOrPause();

                    }
                    break;
                case "PreMusic":
                    preMusic();
                    break;
                case "isPause":
                    playOrPause();
                    break;
                case "isPlaying":
                    playOrPause();
                    break;
                case "NextMusic":
                    nextMusic();
                    break;
                case "ClearMusic":
                    clear(this);
                    break;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void initView() {
        musicUtils = new MusicUtils(this);
        editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.apply();

        musicService = this;

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        ComponentName audiobutton = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());

        audioManager.registerMediaButtonEventReceiver(audiobutton);

        if ((in.resolveActivity(getPackageManager()) == null)) {
            equalizer = new android.media.audiofx.Equalizer(0,
                    mediaPlayer.getAudioSessionId());

            if (MusicUtils.enableEqualizer) {
                equalizer.setEnabled(true);
            } else {
                equalizer.setEnabled(false);
            }
            int num = equalizer.getNumberOfBands();
            SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
            for (int i = 0; i < num && i < Equalizer.Max; i++) {
                int level = sharedPreferences.getInt("equalizer" + i,
                        equalizer.getBandLevel((short) i));
                equalizer.setBandLevel((short) i, (short) level);
            }
        }
    }

    private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (!mediaPlayer.isPlaying() && haveFocus) {
                        haveFocus = false;
                        mediaPlayer.start();
                        createNewNotification(R.drawable.footpause, music);

                        Intent intent1 = new Intent("set PlayOrPause");
                        intent1.putExtra("PlayOrPause", R.drawable.footpausewhite);
                        sendBroadcast(intent1);

                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    haveFocus = false;
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        createNewNotification(R.drawable.footplay, music);

                        Intent intent1 = new Intent("set PlayOrPause");
                        intent1.putExtra("PlayOrPause", R.drawable.footplaywhite);
                        sendBroadcast(intent1);

                    }
                    audioManager.abandonAudioFocus(audioFocusChangeListener);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (mediaPlayer.isPlaying()) {
                        haveFocus = true;
                        mediaPlayer.pause();
                        createNewNotification(R.drawable.footplay, music);


                        Intent intent1 = new Intent("set PlayOrPause");
                        intent1.putExtra("PlayOrPause", R.drawable.footplaywhite);
                        sendBroadcast(intent1);

                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };

    //初始化音乐，准备播放
    private void initMusic(int musicPosition, int progress) {
        if (onPlayingList.size() >= 1) {

            music = onPlayingList.get(musicPosition);

            String uri = music.getUri();
            try {
                File file = new File(uri);
                mediaPlayer.reset();
                mediaPlayer.setDataSource(file.getPath());
                mediaPlayer.prepare();
                mediaPlayer.seekTo(progress);
                canPlay = true;
            } catch (Exception e) {
                Log.d("InitMusic", "init failed");
                Toast.makeText(getApplicationContext(), "Can't play music", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    //播放或暂停
    public void playOrPause() {
        audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_REQUEST_GRANTED);

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();

            Intent intent1 = new Intent("set PlayOrPause");
            intent1.putExtra("PlayOrPause", R.drawable.footplaywhite);
            sendBroadcast(intent1);

            musicUtils.setAlbumCoverToService(getApplicationContext(),
                    music, MusicUtils.FROM_SERVICE);
            createNewNotification(R.drawable.footplay, music);
            haveFocus = false;

        } else {
            mediaPlayer.start();
            musicUtils.setAlbumCoverToService(getApplicationContext(),
                    music, MusicUtils.FROM_SERVICE);
            createNewNotification(R.drawable.footpause, music);

            Intent intent1 = new Intent("set PlayOrPause");
            intent1.putExtra("PlayOrPause", R.drawable.footpausewhite);
            sendBroadcast(intent1);

            haveFocus = true;
        }
        //判断播放模式
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if (isSingleOrCycle == 2) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                createNewNotification(R.drawable.footpause, music);
            } else if (isSingleOrCycle == 3) {
                mediaPlayer.pause();
                createNewNotification(R.drawable.footplay, music);
            } else if (isSingleOrCycle == 1) {
                nextMusic();
                createNewNotification(R.drawable.footpause, music);
            }
        });
        Intent intent1 = new Intent("update");
        sendBroadcast(intent1);
        if (MusicService.mediaPlayer.isPlaying()) {
            if (!MusicUtils.loadWebLyric) {
                try {
                    Mp3File file = new Mp3File(MusicService.music.getUri());
                    if (file.hasId3v2Tag()) {
                        if (file.getId3v2Tag().getLyrics() != null) {
                            Intent intent = new Intent("set lyricButton visibility");
                            intent.putExtra("visibility", View.VISIBLE);
                            sendBroadcast(intent);
                        } else {

                            Intent intent = new Intent("set lyricButton visibility");
                            intent.putExtra("visibility", View.GONE);
                            sendBroadcast(intent);
                            Intent intent3 = new Intent("set lyricView visibility");
                            intent.putExtra("visibility", View.GONE);
                            sendBroadcast(intent3);
                            Intent intent2 = new Intent("set blurBG visibility");
                            intent.putExtra("visibility", View.GONE);
                            sendBroadcast(intent2);
                        }
                    }

                } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                    e.printStackTrace();
                }
            } else {
                Intent intent = new Intent("set lyricButton visibility");
                intent.putExtra("visibility", View.VISIBLE);
                sendBroadcast(intent);
            }
        }
        if ((in.resolveActivity(getPackageManager()) == null)) {
            if (MusicUtils.enableEqualizer)
                equalizer.setEnabled(true);
            else
                equalizer.setEnabled(false);
        }

        editor.putInt("position", musicPosition);
        editor.commit();
    }

    public void nextMusic() {
        audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        mediaPlayer.stop();
        try {

            mediaPlayer.reset();

            if (musicList.size() == 1
                    || musicPosition == musicList.size() - 1) {
                musicPosition = 0;
            } else
                musicPosition++;

            initMusic(musicPosition, 0);
            mediaPlayer.start();

            Intent footIntent = new Intent("set footBar");

            footIntent.putExtra("footTitle", onPlayingList.get(musicPosition).getTitle());
            footIntent.putExtra("footArtist", onPlayingList.get(musicPosition).getArtist());

            createNewNotification(R.drawable.footpause, onPlayingList.get(musicPosition));
            musicUtils.setAlbumCoverToService(getApplicationContext(),
                    onPlayingList.get(musicPosition), MusicUtils.FROM_SERVICE);

            sendBroadcast(footIntent);

            Intent intent1 = new Intent("set PlayOrPause");
            intent1.putExtra("PlayOrPause", R.drawable.footpausewhite);
            sendBroadcast(intent1);

            sendBroadcast(new Intent("Music play to the end"));

        } catch (Exception e) {
            Log.d("hint", "can't jump next music");
            e.printStackTrace();
        }
        Intent intent = new Intent("sendPosition");
        intent.putExtra("position", musicPosition);
        sendBroadcast(intent);

        sendBroadcast(new Intent("update"));
        if (!MusicUtils.loadWebLyric) {
            try {
                Mp3File file = new Mp3File(MusicService.music.getUri());
                if (file.hasId3v2Tag()) {
                    if (file.getId3v2Tag().getLyrics() != null) {

                        Intent intent2 = new Intent("set lyricButton visibility");
                        intent.putExtra("visibility", View.VISIBLE);
                        sendBroadcast(intent2);
                    } else {
                        Intent intent2 = new Intent("set lyricButton visibility");
                        intent.putExtra("visibility", View.GONE);
                        sendBroadcast(intent2);
                    }
                }

            } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                e.printStackTrace();
            }
        }

        sendBroadcast(new Intent("set lyric from service"));

        if ((in.resolveActivity(getPackageManager()) == null)) {
            if (MusicUtils.enableEqualizer)
                equalizer.setEnabled(true);
            else
                equalizer.setEnabled(false);
        }

        editor.putInt("position", musicPosition);
        editor.commit();
    }

    public void preMusic() {
        audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        mediaPlayer.stop();
        try {
            mediaPlayer.reset();
            if (musicPosition == 0)
                musicPosition = musicList.size() - 1;
            else
                musicPosition--;

            initMusic(musicPosition, 0);
            mediaPlayer.start();

            Intent footIntent = new Intent("set footBar");

            footIntent.putExtra("footTitle", onPlayingList.get(musicPosition).getTitle());
            footIntent.putExtra("footArtist", onPlayingList.get(musicPosition).getArtist());
            createNewNotification(R.drawable.footpause, onPlayingList.get(musicPosition));
            musicUtils.setAlbumCoverToService(getApplicationContext(),
                    onPlayingList.get(musicPosition), MusicUtils.FROM_SERVICE);

            sendBroadcast(footIntent);
            Intent intent1 = new Intent("set PlayOrPause");
            intent1.putExtra("PlayOrPause", R.drawable.footpausewhite);
            sendBroadcast(intent1);

            sendBroadcast(new Intent("Music play to the end"));

        } catch (Exception e) {
            Log.d("hint", "can't jump pre music");
            e.printStackTrace();
        }
        Intent intent = new Intent("sendPosition");
        intent.putExtra("position", musicPosition);
        sendBroadcast(intent);

        sendBroadcast(new Intent("update"));
        if (!MusicUtils.loadWebLyric) {
            try {
                Mp3File file = new Mp3File(MusicService.music.getUri());
                if (file.hasId3v2Tag()) {
                    if (file.getId3v2Tag().getLyrics() != null) {
                        Intent intent2 = new Intent("set lyricButton visibility");
                        intent.putExtra("visibility", View.VISIBLE);
                        sendBroadcast(intent2);
                    } else {
                        Intent intent2 = new Intent("set lyricButton visibility");
                        intent.putExtra("visibility", View.GONE);
                        sendBroadcast(intent2);
                    }
                }

            } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                e.printStackTrace();
            }
        }

        sendBroadcast(new Intent("set lyric from service"));

        if ((in.resolveActivity(getPackageManager()) == null)) {
            if (MusicUtils.enableEqualizer)
                equalizer.setEnabled(true);
            else
                equalizer.setEnabled(false);
        }
        editor.putInt("position", musicPosition);
        editor.commit();
    }

    @Override
    public void onDestroy() {

        mediaPlayer.stop();
        mediaPlayer.release();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(1);
        }

        editor.putInt("position", musicPosition);

        editor.putInt("progress", mediaPlayer.getCurrentPosition());
        editor.putInt("flag", 0);
        editor.apply();
        editor.commit();
        MusicUtils.saveArray(this, musicList);

        audioManager.abandonAudioFocus(audioFocusChangeListener);

        unregisterReceiver(musicServiceReceiver);
        super.onDestroy();
    }

    private void createNewNotification(final int id, Music music) {
        createNoti(music.getAlbumArtUri(), id);
    }

    private void createNoti(String uri, int id) {
        Message msg = new Message();

        Bitmap bitmap = GetAlbumArt.getAlbumArtBitmap(getApplicationContext(), uri, 1);

        if (bitmap != null) {
            if (bitmap.getByteCount() > 3000000) {
                bitmap = GetAlbumArt.getAlbumArtBitmap(getApplicationContext(), uri, 5);
            } else if (bitmap.getByteCount() > 2500000) {
                bitmap = GetAlbumArt.getAlbumArtBitmap(getApplicationContext(), uri, 4);
            } else if (bitmap.getByteCount() > 2000000) {
                bitmap = GetAlbumArt.getAlbumArtBitmap(getApplicationContext(), uri, 3);
            } else if (bitmap.getByteCount() > 1250000) {
                bitmap = GetAlbumArt.getAlbumArtBitmap(getApplicationContext(), uri, 2);
            } else {
                bitmap = GetAlbumArt.getAlbumArtBitmap(getApplicationContext(), uri, 1);
            }
        }

        if (bitmap == null) {
            File file = musicUtils.getAlbumCoverFile(music.getArtist(), music.getAlbum());
            if (file.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap.getByteCount() > 3000000) {
                    options.inSampleSize = 5;
                } else if (bitmap.getByteCount() > 2500000) {
                    options.inSampleSize = 4;
                } else if (bitmap.getByteCount() > 2000000) {
                    options.inSampleSize = 3;
                } else if (bitmap.getByteCount() > 1250000) {
                    options.inSampleSize = 2;
                } else {
                    options.inSampleSize = 1;
                }
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !MusicUtils.useOldStyleNotification) {
            notification.createNotification(getApplicationContext(), id, music, bitmap);
        } else {
            if (bitmap != null) {

                Bitmap finalBitmap = bitmap;
                Palette.from(bitmap).generate(palette -> {

                    Palette.Swatch swatch = palette.getVibrantSwatch();
                    if (swatch != null) {
                        msg.what = swatch.getRgb();
                        msg.arg1 = swatch.getTitleTextColor();
                        msg.arg2 = swatch.getBodyTextColor();
                        msg.obj = MusicUtils.messageGood;

                    } else {
                        swatch = palette.getMutedSwatch();
                        if (swatch != null) {
                            msg.what = swatch.getRgb();
                            msg.arg1 = swatch.getTitleTextColor();
                            msg.arg2 = swatch.getBodyTextColor();
                            msg.obj = MusicUtils.messageGood;
                        } else {
                            msg.obj = MusicUtils.messageBad;
                        }
                    }
                    notification.createNotification(getBaseContext(), id,
                            MusicService.music, msg, finalBitmap);
                });
            } else {
                msg.obj = MusicUtils.messageNull;
                notification.createNotification(getBaseContext(), id,
                        MusicService.music, msg, null);
            }
        }

    }

    class MusicServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                        if (mediaPlayer.isPlaying() && isPlug) {
                            mediaPlayer.pause();

                            Intent intent1 = new Intent("set PlayOrPause");
                            intent1.putExtra("PlayOrPause", R.drawable.footplaywhite);
                            sendBroadcast(intent1);

                            createNewNotification(R.drawable.footplay, music);
                        }
                        break;
                    case AudioManager.ACTION_HEADSET_PLUG:
                        if (intent.hasExtra("state")) {
                            isPlug = intent.getIntExtra("state", 0) != 0;
                        }
                        break;
                    case "STOP":
                        mediaPlayer.stop();
                        Log.d("STOP", "STOP");
                        break;
                    case "PlAYORPAUSE":
                        if (canPlay)
                            playOrPause();
                        else {
                            initView();

                            if (MusicListFragment.count == 1) {
                                musicList = MusicListFragment.musicList;
                            } else if (ArtistDetailFragment.count == 1) {
                                musicList = ArtistDetailFragment.musicList;
                            } else if (AlbumDetailFragment.count == 1) {
                                musicList = AlbumDetailFragment.musicList;
                            } else if (PlaylistDetailFragment.count == 1) {
                                musicList = PlaylistDetailFragment.musicList;
                            } else if (FolderDetailFragment.count == 1) {
                                musicList = FolderDetailFragment.musicList;
                            } else if (MusicRecentAddedFragment.count == 1) {
                                musicList = MusicRecentAddedFragment.musicList;
                            } else {
                                musicList = MusicList.list;
                            }

                            if (isRandom) {
                                onPlayingList = shuffleList;
                            } else {
                                onPlayingList = musicList;
                            }

                            musicUtils = new MusicUtils(getApplicationContext());
                            initMusic(MusicUtils.pos, 0);
                            playOrPause();
                        }
                        Log.d("PlAYORPAUSE", "PlAYORPAUSE");
                        break;
                    case "PREVIOUS":
                        preMusic();
                        Log.d("PREVIOUS", "PREVIOUS");
                        break;
                    case "NEXT":
                        nextMusic();
                        Log.d("NEXT", "NEXT");
                        break;
                    case "setPlayOrPause":
                        createNewNotification(intent.getIntExtra("playOrPause",
                                R.drawable.footpause), music);
                        Log.d("setPlayOrPause", "setPlayOrPause");
                        break;
                    case "enableColorNotification":
                        createNewNotification(intent.getIntExtra("playOrPause",
                                R.drawable.footpause), music);
                        Log.d("enableColorNotification", "enableColorNotification");
                        break;
                    case "setNotification":
                        createNewNotification(R.drawable.footpause, music);
                        break;
                    case "refresh notification":
                        String uri = intent.getStringExtra("file");
                        if (mediaPlayer.isPlaying())
                            createNoti(uri, R.drawable.footpause);
                        else
                            createNoti(uri, R.drawable.footplay);
                        Log.d("refresh notification", "refresh notification");
                        break;
                    case "pause music":
                        createNewNotification(R.drawable.footplay, music);
                        break;
                    case "delete current music success":
                        nextMusic();
                        break;
                    case "clear":
                        clear(context);
                        break;
                    case "cycle list":
                        switch (intent.getIntExtra("from", 1)) {
                            case 1:
                                musicList = MusicListFragment.musicList;
                                break;
                            case 2:
                                musicList = ArtistDetailFragment.musicList;
                                break;
                            case 3:
                                musicList = AlbumDetailFragment.musicList;
                                break;
                            case 4:
                                musicList = PlaylistDetailFragment.musicList;
                                break;
                            case 5:
                                musicList = FolderDetailFragment.musicList;
                                break;
                            case 6:
                                musicList = MusicRecentAddedFragment.musicList;
                                break;
                            case 7:
                                musicList = SearchActivity.musicList;
                                break;
                            case 8:
                                musicList = MusicList.list;
                                break;
                        }
                        MusicUtils.saveArray(context, musicList);
                        break;
                    case "random play":
                        shuffleList = musicUtils.createShuffleList(musicList);
                        onPlayingList = shuffleList;

                        int p = 0;
                        for (int i = 0; i < shuffleList.size(); i++) {
                            if (shuffleList.get(i).getUri().equals(music.getUri())) {
                                p = i;
                                break;
                            }
                        }
                        Music temp = shuffleList.get(0);
                        shuffleList.set(0, shuffleList.get(p));
                        shuffleList.set(musicPosition, temp);

                        musicPosition = 0;
                        editor.putInt("position", musicPosition);
                        editor.apply();
                        editor.commit();
                        MusicUtils.saveShuffleArray(context, shuffleList);
                        break;
                    case "cycle play":
                        for (int i = 0; i < shuffleList.size(); i++) {
                            if (musicList.get(i).getUri()
                                    .equals(shuffleList.get(musicPosition).getUri())) {
                                musicPosition = i;
                                editor.putInt("position", musicPosition);
                                editor.apply();
                                editor.commit();
                                break;
                            }
                        }
                        onPlayingList = musicList;
                        break;
                    case "play next":
                        String musicUri = intent.getStringExtra("uri");
                        int p1 = 0;
                        if (onPlayingList != null && onPlayingList.size() > 0) {
                            for (int i = 0; i < onPlayingList.size(); i++) {
                                if (onPlayingList.get(i).getUri().equals(musicUri)) {
                                    p1 = i;
                                    break;
                                }
                            }
                            Music temp1 = onPlayingList.get(p1);
                            onPlayingList.remove(p1);
                            if (p1 > musicPosition)
                                onPlayingList.add(musicPosition + 1, temp1);
                            else if (p1 < musicPosition) {
                                onPlayingList.add(musicPosition, temp1);
                                musicPosition--;
                            } else {
                                onPlayingList.add(musicPosition, temp1);
                            }
                            MusicUtils.saveArray(context, musicList);
                            MusicUtils.saveShuffleArray(context, shuffleList);
                        } else if (isRandom) {
                            for (int i = 0; i < MusicList.shufflelist.size(); i++) {
                                if (MusicList.shufflelist.get(i).getUri().equals(musicUri)) {
                                    p1 = i;
                                    break;
                                }
                            }
                            Music temp1 = MusicList.shufflelist.get(p1);
                            MusicList.shufflelist.remove(p1);
                            if (p1 > MusicUtils.pos)
                                MusicList.shufflelist.add(MusicUtils.pos + 1, temp1);
                            else if (p1 < MusicUtils.pos) {
                                MusicList.shufflelist.add(MusicUtils.pos, temp1);
                                MusicUtils.pos--;
                            } else {
                                MusicList.shufflelist.add(MusicUtils.pos, temp1);
                            }
                            MusicUtils.saveShuffleArray(context, MusicList.shufflelist);
                        } else {
                            for (int i = 0; i < MusicList.list.size(); i++) {
                                if (MusicList.list.get(i).getUri().equals(musicUri)) {
                                    p1 = i;
                                    break;
                                }
                            }
                            Music temp1 = MusicList.list.get(p1);
                            MusicList.list.remove(p1);
                            if (p1 > MusicUtils.pos)
                                MusicList.list.add(MusicUtils.pos + 1, temp1);
                            else if (p1 < MusicUtils.pos) {
                                MusicList.list.add(MusicUtils.pos, temp1);
                                MusicUtils.pos--;
                            } else {
                                MusicList.list.add(MusicUtils.pos, temp1);
                            }
                            MusicUtils.saveArray(context, MusicList.list);
                        }
                        break;
                }
            }
        }
    }

    private void clear(Context context) {
        Intent intent1 = new Intent("set PlayOrPause");
        intent1.putExtra("PlayOrPause", R.drawable.footplaywhite);
        sendBroadcast(intent1);

        mediaPlayer.pause();
        MusicList.flag = 0;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(1);
        }
        stopForeground(true);


        editor.putInt("position", musicPosition);

        editor.putInt("progress", mediaPlayer.getCurrentPosition());
        editor.putInt("flag", 0);
        editor.apply();
        editor.commit();

        MusicUtils.saveArray(context, musicList);
    }
}

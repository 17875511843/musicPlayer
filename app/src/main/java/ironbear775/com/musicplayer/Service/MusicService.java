package ironbear775.com.musicplayer.Service;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
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
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import ironbear775.com.musicplayer.Activity.BaseActivity;
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
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    public static boolean isRandom = false;
    public static int isSingleOrCycle = 1;//1为cycle 2single 3 none
    public static MusicService musicService;

    private Notification notification;
    private AudioManager audioManager;
    private boolean haveFocus;
    private boolean[] isPlug = {false, false};
    private int plugTime = 0;
    private boolean isPlugStateChange = false;
    private boolean isAudioFocusLossTransient = false;
    private android.media.audiofx.Equalizer equalizer;
    private SharedPreferences.Editor editor;
    private MusicServiceReceiver musicServiceReceiver;
    private boolean canPlay = false;
    public static boolean isBluetoothHeadsetConnected = false;

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
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        musicPosition = sharedPreferences.getInt("position", 0);

        musicServiceReceiver = new MusicServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
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
        intentFilter.addAction("destroy service");
        intentFilter.addAction("save data");
        registerReceiver(musicServiceReceiver, intentFilter);

        initView();
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

        int result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        notification = new Notification(this);
        try {
            if (intent.getAction() != null) {
                String action = intent.getAction();
                switch (action) {
                    case "musiclist":
                        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            int from = intent.getIntExtra("from", 1);
                            switch (from) {
                                case 1:
                                    musicList = MusicListFragment.musicList; //音乐页面的列表
                                    break;
                                case 2:
                                    musicList = ArtistDetailFragment.musicList; //艺术家详情页面的列表
                                    break;
                                case 3:
                                    musicList = AlbumDetailFragment.musicList; //专辑详情页面的列表
                                    break;
                                case 4:
                                    musicList = PlaylistDetailFragment.musicList; //播放列表详情页面的列表
                                    break;
                                case 5:
                                    musicList = FolderDetailFragment.musicList; //文件夹详情页面的列表
                                    break;
                                case 6:
                                    musicList = MusicRecentAddedFragment.musicList; //最近添加页面的列表
                                    break;
                                case 7:
                                    musicList = SearchActivity.musicList; //搜索页面的列表
                                    break;
                                case 8:
                                    musicList = MusicList.list; //直接点击底部播放栏，列表为应用上次保存的列表
                                    break;
                                case 9:
                                    //随机播放传入的列表
                                    shuffleList = MusicUtils.getInstance().getInstance().arrayList;
                                    break;
                                case 10:
                                    //外部应用调起传入的列表，list size为1
                                    musicList = MusicList.openlist;
                                    break;

                            }
                            musicPosition = intent.getIntExtra("musicPosition", 0);
                            if (from != 11) {
                                if (isRandom) { //播放模式是否为随机播放
                                    if (from != 9 && from != 8) {
                                        //根据传入的列表生产随机播放列表
                                        shuffleList = MusicUtils.getInstance().createShuffleList(musicList);
                                        int p = 0;
                                        //获取即将播放的歌曲在生成随机列表的位置
                                        for (int i = 0; i < shuffleList.size(); i++) {
                                            if (shuffleList.get(i).getUri().equals(
                                                    musicList.get(musicPosition).getUri())) {
                                                p = i;
                                                break;
                                            }
                                        }
                                        //将该歌曲放置在列表首部，第一个播放
                                        Music temp = shuffleList.get(0);
                                        shuffleList.set(0, shuffleList.get(p));
                                        shuffleList.set(p, temp);

                                    } else if (from == 8) {
                                        //直接点击底部播放栏，正在播放列表为应用上次保存的列表
                                        shuffleList = MusicUtils.getInstance().getShuffleArray(this);
                                    }
                                    if (shuffleList != null && shuffleList.size() > 0) {
                                        //正在播放列表为生成的随机列表
                                        onPlayingList = shuffleList;
                                        if (from != 8)
                                            musicPosition = 0;
                                    } else
                                        onPlayingList = musicList;
                                } else {
                                    if (from == 8)
                                        //正在播放列表为获取保存的列表
                                        onPlayingList = MusicUtils.getInstance().getArray(this);
                                    else
                                        //正在播放列表为传入的列表
                                        onPlayingList = musicList;
                                }

                            }

                            //对本次的正在播放列表进行保存，供下次打开应用时使用
                            if (shuffleList == null || shuffleList.size() == 0)
                                MusicUtils.getInstance().saveShuffleArray(this, musicList);
                            else
                                MusicUtils.getInstance().saveShuffleArray(this, shuffleList);

                            MusicUtils.getInstance().saveArray(this, musicList);

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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void initView() {
        editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.apply();

        musicService = this;

        mediaPlayer = new MediaPlayer();

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        ComponentName audiobutton = new ComponentName(getPackageName(),
                MediaButtonReceiver.class.getName());

        audioManager.registerMediaButtonEventReceiver(audiobutton);

        if (in.resolveActivity(getPackageManager()) == null && !isBluetoothHeadsetConnected) {
            equalizer = new android.media.audiofx.Equalizer(0,
                    mediaPlayer.getAudioSessionId());

            if (MusicUtils.getInstance().enableEqualizer) {
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
                    if (!mediaPlayer.isPlaying() && haveFocus && !isPlugStateChange) {
                        haveFocus = false;
                        mediaPlayer.start();
                        createNewNotification(R.drawable.footpause, music);

                        Intent intent1 = new Intent("set PlayOrPause");
                        intent1.putExtra("PlayOrPause", R.drawable.pause_to_play_white_anim);

                        if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                            intent1.putExtra("play_Pause", R.drawable.pause_to_play_white_anim);
                        else
                            intent1.putExtra("play_Pause", R.drawable.pause_to_play_anim);

                        sendBroadcast(intent1);
                    } else {
                        isPlugStateChange = false;
                        isAudioFocusLossTransient = false;
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    haveFocus = false;
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        createNewNotification(R.drawable.footplay, music);

                        Intent intent1 = new Intent("set PlayOrPause");
                        intent1.putExtra("PlayOrPause", R.drawable.pause_to_play_white_anim);

                        if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                            intent1.putExtra("play_Pause", R.drawable.pause_to_play_white_anim);
                        else
                            intent1.putExtra("play_Pause", R.drawable.pause_to_play_anim);

                        sendBroadcast(intent1);

                    }
                    audioManager.abandonAudioFocus(audioFocusChangeListener);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    isAudioFocusLossTransient = true;
                    if (mediaPlayer.isPlaying()) {
                        haveFocus = true;
                        mediaPlayer.pause();
                        createNewNotification(R.drawable.footplay, music);


                        Intent intent1 = new Intent("set PlayOrPause");
                        intent1.putExtra("PlayOrPause", R.drawable.play_to_pause_white_anim);

                        if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                            intent1.putExtra("play_Pause", R.drawable.play_to_pause_white_anim);
                        else
                            intent1.putExtra("play_Pause", R.drawable.play_to_pause_anim);

                        sendBroadcast(intent1);

                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.d("AUDIOFOCUSLOSSTRANSIENT", "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: ");
                    break;
            }
        }
    };

    //初始化音乐，准备播放
    private void initMusic(int musicPosition, int progress) {
        if (onPlayingList.size() >= 1) {

            music = onPlayingList.get(musicPosition); //获取当前需要播放的歌曲

            String uri = music.getUri();//歌曲的数据库地址
            try {
                File file = new File(uri);//打开文件
                mediaPlayer.reset(); //重置MediaPlayer
                mediaPlayer.setDataSource(file.getPath()); //设置播放源
                mediaPlayer.prepare();//准备播放
                mediaPlayer.seekTo(progress);//跳到歌曲需要播放的位置
                canPlay = true;
            } catch (Exception e) {
                //播放错误的操作
                Log.d("InitMusic", "init failed");
                Toast.makeText(getApplicationContext(), "Can't play music", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    //播放或暂停
    public void playOrPause() {
        //获取播放焦点，这样才可以将其他正在播放音频的应用的播放权抢夺过来
        audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED);

        MusicList.flag = 1;

        if (mediaPlayer.isPlaying()) {//已经在播放
            mediaPlayer.pause();

            Intent intent1 = new Intent("set PlayOrPause");
            intent1.putExtra("PlayOrPause", R.drawable.play_to_pause_white_anim);

            if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                intent1.putExtra("play_Pause", R.drawable.play_to_pause_white_anim);
            else
                intent1.putExtra("play_Pause", R.drawable.play_to_pause_anim);

            sendBroadcast(intent1);

            MusicUtils.getInstance().setAlbumCoverToService(getApplicationContext(),
                    music, MusicUtils.getInstance().FROM_SERVICE);
            createNewNotification(R.drawable.footplay, music);
            haveFocus = false;

        } else {
            mediaPlayer.start();
            MusicUtils.getInstance().setAlbumCoverToService(getApplicationContext(),
                    music, MusicUtils.getInstance().FROM_SERVICE);

            createNewNotification(R.drawable.footpause, music);

            Intent intent1 = new Intent("set PlayOrPause");
            intent1.putExtra("PlayOrPause", R.drawable.pause_to_play_white_anim);

            if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                intent1.putExtra("play_Pause", R.drawable.pause_to_play_white_anim);
            else
                intent1.putExtra("play_Pause", R.drawable.pause_to_play_anim);

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
        sendBroadcast(new Intent("update onPlayingList"));
        sendBroadcast(new Intent("update"));

        if ((in.resolveActivity(getPackageManager()) == null) && !isBluetoothHeadsetConnected) {
            if (MusicUtils.getInstance().enableEqualizer)
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
            MusicUtils.getInstance().setAlbumCoverToService(getApplicationContext(),
                    onPlayingList.get(musicPosition), MusicUtils.getInstance().FROM_SERVICE);

            sendBroadcast(footIntent);

            Intent intent1 = new Intent("set PlayOrPause");
            intent1.putExtra("PlayOrPause", R.drawable.pause_to_play_white_anim);

            if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                intent1.putExtra("play_Pause", R.drawable.pause_to_play_white_anim);
            else
                intent1.putExtra("play_Pause", R.drawable.pause_to_play_anim);

            sendBroadcast(intent1);

            sendBroadcast(new Intent("Music play to the end"));

        } catch (Exception e) {
            Log.d("hint", "can't jump next music");
            e.printStackTrace();
        }
        Intent intent = new Intent("sendPosition");
        intent.putExtra("position", musicPosition);
        sendBroadcast(intent);

        sendBroadcast(new Intent("update onPlayingList"));
        sendBroadcast(new Intent("update"));
        sendBroadcast(new Intent("set lyric from service"));

        if ((in.resolveActivity(getPackageManager()) == null) && !isBluetoothHeadsetConnected) {
            if (MusicUtils.getInstance().enableEqualizer)
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
            MusicUtils.getInstance().setAlbumCoverToService(getApplicationContext(),
                    onPlayingList.get(musicPosition), MusicUtils.getInstance().FROM_SERVICE);

            sendBroadcast(footIntent);
            Intent intent1 = new Intent("set PlayOrPause");
            intent1.putExtra("PlayOrPause", R.drawable.pause_to_play_white_anim);

            if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                intent1.putExtra("play_Pause", R.drawable.pause_to_play_white_anim);
            else
                intent1.putExtra("play_Pause", R.drawable.pause_to_play_anim);

            sendBroadcast(intent1);

            sendBroadcast(new Intent("Music play to the end"));

        } catch (Exception e) {
            Log.d("hint", "can't jump pre music");
            e.printStackTrace();
        }
        Intent intent = new Intent("sendPosition");
        intent.putExtra("position", musicPosition);
        sendBroadcast(intent);

        sendBroadcast(new Intent("update onPlayingList"));
        sendBroadcast(new Intent("update"));
        sendBroadcast(new Intent("set lyric from service"));

        if ((in.resolveActivity(getPackageManager()) == null) && !isBluetoothHeadsetConnected) {
            if (MusicUtils.getInstance().enableEqualizer)
                equalizer.setEnabled(true);
            else
                equalizer.setEnabled(false);
        }

        editor.putInt("position", musicPosition);
        editor.commit();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        clear(this);
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        } catch (Exception ignore) {

        }

        if (equalizer != null)
            equalizer.release();
        unregisterReceiver(musicServiceReceiver);
    }


    private void createNewNotification(final int id, Music music) {
        if (music != null)
            createNoti(music.getAlbumArtUri(), id);
    }

    private void createNoti(String uri, int id) {
        Message msg = new Message();

        if (notification == null)
            notification = new Notification(this);

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

        if (bitmap == null && music != null) {
            File file = MusicUtils.getInstance().getAlbumCoverFile(music.getArtist(), music.getAlbum());
            BitmapFactory.Options options = new BitmapFactory.Options();
            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap!= null) {
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
            } else {
                options.inSampleSize = 5;
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art, options);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !MusicUtils.getInstance().useOldStyleNotification && music != null) {
            notification.createNotification(getApplicationContext(), id, music, bitmap);
        } else {
            if (MusicService.music != null) {
                if (bitmap != null) {

                    Bitmap finalBitmap = bitmap;
                    Palette.from(bitmap).generate(palette -> {

                        Palette.Swatch swatch = palette.getVibrantSwatch();
                        if (swatch != null) {
                            msg.what = swatch.getRgb();
                            msg.arg1 = swatch.getTitleTextColor();
                            msg.arg2 = swatch.getBodyTextColor();
                            msg.obj = MusicUtils.getInstance().messageGood;

                        } else {
                            swatch = palette.getMutedSwatch();
                            if (swatch != null) {
                                msg.what = swatch.getRgb();
                                msg.arg1 = swatch.getTitleTextColor();
                                msg.arg2 = swatch.getBodyTextColor();
                                msg.obj = MusicUtils.getInstance().messageGood;
                            } else {
                                msg.obj = MusicUtils.getInstance().messageBad;
                            }
                        }
                        notification.createNotification(getBaseContext(), id,
                                MusicService.music, msg, finalBitmap);
                    });
                } else {
                    msg.obj = MusicUtils.getInstance().messageNull;
                    notification.createNotification(getBaseContext(), id,
                            MusicService.music, msg, null);
                }
            }
        }

    }

    private void setPlayOrPause() {
        initView();

        switch (MusicUtils.getInstance().playPage) {
            case 1:
                musicList = MusicListFragment.musicList;
                break;
            case 2:
                musicList = MusicRecentAddedFragment.musicList;
                break;
            case 3:
                musicList = ArtistDetailFragment.musicList;
                break;
            case 4:
                musicList = AlbumDetailFragment.musicList;
                break;
            case 5:
                musicList = PlaylistDetailFragment.musicList;
                break;
            case 6:
                musicList = FolderDetailFragment.musicList;
                break;
            case 7:
                musicList = MusicList.list;
                break;
        }

        if (shuffleList == null || shuffleList.size() == 0) {
            shuffleList = MusicUtils.getInstance().createShuffleList(musicList);
        }

        if (isRandom) {
            onPlayingList = shuffleList;
        } else {
            onPlayingList = musicList;
        }

        initMusic(MusicUtils.getInstance().pos, musicPosition);
        playOrPause();
    }

    class MusicServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                        if (BluetoothProfile.STATE_CONNECTED ==
                                adapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                            isBluetoothHeadsetConnected = true;
                        } else if (BluetoothProfile.STATE_DISCONNECTED ==
                                adapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                            isBluetoothHeadsetConnected = false;
                        }
                        Log.d("isBluetoothHeadset", "isBluetoothHeadsetConnected: " + isBluetoothHeadsetConnected);
                        break;
                    case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();

                            Intent intent1 = new Intent("set PlayOrPause");
                            intent1.putExtra("PlayOrPause",
                                    R.drawable.play_to_pause_white_anim);

                            if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                                intent1.putExtra("play_Pause",
                                        R.drawable.play_to_pause_white_anim);
                            else
                                intent1.putExtra("play_Pause",
                                        R.drawable.play_to_pause_anim);

                            sendBroadcast(intent1);

                            createNewNotification(R.drawable.footplay, music);
                        }
                        break;
                    case AudioManager.ACTION_HEADSET_PLUG:
                        BluetoothAdapter adapter1 = BluetoothAdapter.getDefaultAdapter();
                        if (BluetoothProfile.STATE_CONNECTED ==
                                adapter1.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                            isBluetoothHeadsetConnected = true;
                        }

                        if (intent.hasExtra("state")) {
                            isPlug[plugTime] = intent.getIntExtra("state", 0) != 0;

                            if (plugTime == 0)
                                plugTime++;
                            else {
                                plugTime--;
                                if (isPlug[0] != isPlug[1] && isAudioFocusLossTransient)
                                    isPlugStateChange = true;
                            }
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
                            setPlayOrPause();
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
                        MusicUtils.getInstance().saveArray(context, musicList);
                        break;
                    case "random play":
                        shuffleList = MusicUtils.getInstance().createShuffleList(musicList);
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
                        MusicUtils.getInstance().saveShuffleArray(context, shuffleList);
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
                            MusicUtils.getInstance().saveArray(context, musicList);
                            MusicUtils.getInstance().saveShuffleArray(context, shuffleList);
                        } else if (isRandom) {
                            for (int i = 0; i < MusicList.shufflelist.size(); i++) {
                                if (MusicList.shufflelist.get(i).getUri().equals(musicUri)) {
                                    p1 = i;
                                    break;
                                }
                            }
                            Music temp1 = MusicList.shufflelist.get(p1);
                            MusicList.shufflelist.remove(p1);
                            if (p1 > MusicUtils.getInstance().pos)
                                MusicList.shufflelist.add(MusicUtils.getInstance().pos + 1, temp1);
                            else if (p1 < MusicUtils.getInstance().pos) {
                                MusicList.shufflelist.add(MusicUtils.getInstance().pos, temp1);
                                MusicUtils.getInstance().pos--;
                            } else {
                                MusicList.shufflelist.add(MusicUtils.getInstance().pos, temp1);
                            }
                            MusicUtils.getInstance().saveShuffleArray(context, MusicList.shufflelist);
                        } else {
                            for (int i = 0; i < MusicList.list.size(); i++) {
                                if (MusicList.list.get(i).getUri().equals(musicUri)) {
                                    p1 = i;
                                    break;
                                }
                            }
                            Music temp1 = MusicList.list.get(p1);
                            MusicList.list.remove(p1);
                            if (p1 > MusicUtils.getInstance().pos)
                                MusicList.list.add(MusicUtils.getInstance().pos + 1, temp1);
                            else if (p1 < MusicUtils.getInstance().pos) {
                                MusicList.list.add(MusicUtils.getInstance().pos, temp1);
                                MusicUtils.getInstance().pos--;
                            } else {
                                MusicList.list.add(MusicUtils.getInstance().pos, temp1);
                            }
                            MusicUtils.getInstance().saveArray(context, MusicList.list);
                        }
                        break;
                    case "save data":
                        clear(context);
                        Log.d("save data", "save data");
                        stopSelf();
                        System.exit(0);
                        break;
                }
            }
        }

    }

    private void clear(Context context) {
        Intent intent1 = new Intent("set PlayOrPause");
        intent1.putExtra("PlayOrPause", R.drawable.play_to_pause_white_anim);

        if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
            intent1.putExtra("play_Pause", R.drawable.play_to_pause_white_anim);
        else
            intent1.putExtra("play_Pause", R.drawable.play_to_pause_anim);

        sendBroadcast(intent1);

        try {
            if (mediaPlayer.isPlaying())
                mediaPlayer.pause();
            MusicList.flag = 0;

            editor.putInt("position", musicPosition);
            editor.putInt("progress", mediaPlayer.getCurrentPosition());
            editor.putInt("flag", 0);
            editor.apply();
            editor.commit();

            MusicUtils.getInstance().saveArray(context, musicList);
            MusicUtils.getInstance().saveShuffleArray(context, shuffleList);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(1);
            }
            stopForeground(true);
        } catch (Exception ignored) {

        }

    }
}

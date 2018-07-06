package ironbear775.com.musicplayer.Activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.beaglebuddy.mp3.MP3;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondarySwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ironbear775.com.musicplayer.Class.Music;
import ironbear775.com.musicplayer.Fragment.AlbumDetailFragment;
import ironbear775.com.musicplayer.Fragment.AlbumListFragment;
import ironbear775.com.musicplayer.Fragment.ArtistDetailFragment;
import ironbear775.com.musicplayer.Fragment.ArtistListFragment;
import ironbear775.com.musicplayer.Fragment.FolderDetailFragment;
import ironbear775.com.musicplayer.Fragment.FolderFragment;
import ironbear775.com.musicplayer.Fragment.MusicListFragment;
import ironbear775.com.musicplayer.Fragment.MusicRecentAddedFragment;
import ironbear775.com.musicplayer.Fragment.PlaylistDetailFragment;
import ironbear775.com.musicplayer.Fragment.PlaylistFragment;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MediaButtonReceiver;
import ironbear775.com.musicplayer.Service.MusicService;
import ironbear775.com.musicplayer.Util.DetailDialog;
import ironbear775.com.musicplayer.Util.FootBarRelativeLayout;
import ironbear775.com.musicplayer.Util.GetAlbumArt;
import ironbear775.com.musicplayer.Util.Lyric.LrcView;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.OnPlayingListDialog;
import ironbear775.com.musicplayer.Util.PlaylistDbHelper;
import ironbear775.com.musicplayer.Util.PlaylistDialog;
import ironbear775.com.musicplayer.Util.SquareImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by ironbear on 2016/12/9.
 */

public class MusicList extends BaseActivity implements Serializable, View.OnClickListener
        , ActionMode.Callback {

    public static Fragment albumFragment;
    public static Fragment artistFragment;
    public static Fragment playlistFragment;
    public static Fragment folderFragment;
    private Fragment musicRecentFragment;
    private Fragment musicFragment;
    public static int Mod = 1;
    public static ActionMode actionMode;
    public static boolean isAlbum = false;
    public static boolean isArtist = false;
    public static int flag = 0;
    public static ArrayList<Music> list;
    public static ArrayList<Music> openlist;
    public static ArrayList<Music> shufflelist;
    public static Set<Integer> listPositionSet = new HashSet<>();
    public static String fromWhere;
    public static String artistInAlbum;
    public static boolean first = true;
    public static int statusBarColor;
    public static int colorPri;

    private View headerView;
    private TextView footTitle;
    private TextView footArtist;
    private TextView headerTitle;
    private ImageView headerAlbumArt;
    private ImageView footAlbumArt;
    private com.github.clans.fab.FloatingActionButton PlayOrPause, play_pause;
    private ImageView slideMenu;
    private SlideUp slideUp;
    private MusicService musicService;
    public Toolbar toolbar;
    private SquareImageView mainImageView;
    private TextView title, artist, album, duration, current;
    private ImageView cyclePlay;
    private ImageView randomPlay;
    private SeekBar musicProgress;
    private FootBarRelativeLayout footBar;
    private Drawer slideDrawer;
    private FragmentManager fragmentManager;
    private ArrayList<Music> musicList = new ArrayList<>();
    private FragmentTransaction transaction;
    private View slideView;
    private LrcView lyricView;
    private ImageView onPlayingListButton;
    private SquareImageView blurBG;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private MusicFinishReceiver musicFinishReceiver;
    private final SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    private int progress = 0;
    private float xUp, yDown, yUp, xDown;
    private float density;
    private String nowPlayingAlbum, nowPlayingArtist;
    private boolean backWasPressedInActionMode = false;
    private boolean actionModeIsActive = false;
    private ImageView last, next;
    private AnimatedVectorDrawable playToPauseDrawable, pauseToPlayDrawable;
    private AnimatedVectorDrawable playToPauseWhiteDrawable, pauseToPlayWhiteDrawable;
    private boolean isConn;

    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MusicBinder) iBinder).getService();
            isConn = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isConn = false;
        }
    };

    public MusicList() {
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.music_list_layout);

        Resources.Theme theme = getTheme();
        TypedValue colorPrimaryValue = new TypedValue();

        theme.resolveAttribute(R.attr.colorPrimary, colorPrimaryValue, true);
        Resources resources = getResources();

        colorPri = ResourcesCompat.getColor(resources,
                colorPrimaryValue.resourceId, null);

        Boolean isNewVersion = getIntent().getBooleanExtra("IS_NEW_VERSION", false);

        if (isNewVersion) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.new_version_title);
            builder.setMessage(R.string.new_version_text);
            builder.setPositiveButton(R.string.delete_confrim, (dialogInterface, i) -> {
            });
            builder.show();
        }

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.apply();

        DisplayMetrics dm;
        dm = getResources().getDisplayMetrics();
        density = dm.density;

        MusicUtils.getInstance().isFlyme = MusicUtils.getInstance().isFlyme(this);

        musicFinishReceiver = new MusicFinishReceiver();

        registerReceiver(musicFinishReceiver, musicFinishFilter());

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        ComponentName audioButton = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());

        if (audioManager != null) {
            audioManager.registerMediaButtonEventReceiver(audioButton);
        }

        findView();

        slideUp = new SlideUpBuilder(slideView)
                .withStartGravity(Gravity.BOTTOM)
                .withGesturesEnabled(true)
                .withTouchableAreaDp(1000)
                .withGesturesEnabled(true)
                .withStartState(SlideUp.State.HIDDEN)
                .withListeners(new SlideUp.Listener.Events() {
                    @Override
                    public void onSlide(float percent) {
                        slideView.setAlpha(1 - (percent / 100));
                    }

                    @Override
                    public void onVisibilityChanged(int visibility) {
                        if (visibility == View.GONE) {

                            Intent intent = new Intent("SetClickable_True");
                            sendBroadcast(intent);

                            MusicUtils.getInstance().cancelNetCall();
                            lyricView.setVisibility(View.GONE);
                            blurBG.setVisibility(View.GONE);
                            if (MusicUtils.getInstance().isPlayed && footBar.getVisibility() == View.GONE) {
                                footBar.setVisibility(View.VISIBLE);
                                PlayOrPause.show(true);
                            }
                        }
                    }
                })
                .build();
        slideUp.hideImmediately();

        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        init();

                        toolbar.inflateMenu(R.menu.menu_short);

                        if (getIntent().getBooleanExtra("Start Activity", false)) {
                            sendBroadcast(new Intent("open activity"));
                        }

                        footBar.setVisibility(View.GONE);
                        PlayOrPause.hide(false);
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        Toast.makeText(MusicList.this, R.string.need_permission, Toast.LENGTH_LONG)
                                .show();
                        removeALLActivity();
                    }
                })
                .start();


    }


    private void openMusicFromFile(Intent intent) {
        String path = intent.getStringExtra("path");
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Audio.Media.DATA + "=?",
                new String[]{path},
                MediaStore.Audio.Media.TITLE);

        Music music = new Music();
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                music.setID(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                music.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));

                music.setAlbumArtUri(String.valueOf(ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart")
                        , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))));
                music.setUri(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                music.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                music.setAlbum_id(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                music.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                music.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                music.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                cursor.close();
            }

            startPlay(music);

        } catch (Exception e) {
            File file = new File(path);
            if (file.getAbsolutePath().contains(".mp3") || file.getAbsolutePath().contains(".MP3")) {
                try {
                    Mp3File mp3File = new Mp3File(file);
                    ID3v2 tag = mp3File.getId3v2Tag();
                    if (tag != null) {
                        music.setUri(path);
                        music.setTitle(tag.getTitle());
                        music.setAlbum(tag.getAlbum());
                        music.setArtist(tag.getArtist());
                        music.setDuration(tag.getLength());

                        startPlay(music);

                    } else {
                        Toast.makeText(MusicList.this, R.string.unknown_error, Toast.LENGTH_SHORT)
                                .show();
                    }

                } catch (IOException | UnsupportedTagException | InvalidDataException e1) {
                    Toast.makeText(MusicList.this, R.string.unknown_error, Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                music.setUri(path);
                int index = path.lastIndexOf("/");
                music.setTitle(path.substring(index + 1));
                startPlay(music);

            }
        }

    }

    private void startPlay(Music music) {

        editor.putBoolean("isPlayed", true);
        editor.apply();

        footBar.setVisibility(View.VISIBLE);
        PlayOrPause.show(true);

        startMusic(music);
        footTitle.setText(music.getTitle());
        footArtist.setText(music.getArtist());

        if (slideUp.isVisible()) {
            setAlbumCoverToMainImageView(MusicList.this, music,
                    MusicUtils.getInstance().FROM_MAINIMAGE);
        }
    }

    /**
     * 初始化操作
     */
    private void init() {

        first = sharedPreferences.getBoolean("firstTime", true);

        if (!first) {
            list = MusicUtils.getInstance().getArray(this);
            shufflelist = MusicUtils.getInstance().getShuffleArray(this);

        }

        statusBarColor = getWindow().getStatusBarColor();

        MusicUtils.getInstance().pos = sharedPreferences.getInt("position", 0);
        progress = sharedPreferences.getInt("progress", 0);
        MusicService.isRandom = sharedPreferences.getBoolean("isRandom", false);
        MusicService.isSingleOrCycle = sharedPreferences.getInt("isSingleOrCycle", 1);
        flag = sharedPreferences.getInt("flag", 0);
        //MusicUtils.getInstance().playPage = sharedPreferences.getInt("playPage",0);
        MusicUtils.getInstance().enableDefaultCover = sharedPreferences.getBoolean("enableDefaultCover", false);
        MusicUtils.getInstance().enableColorNotification = sharedPreferences.getBoolean("enableColorNotification", false);
        MusicUtils.getInstance().enableEqualizer = sharedPreferences.getBoolean("enableEqualizer", false);
        MusicUtils.getInstance().useOldStyleNotification = sharedPreferences.getBoolean("useOldStyleNotification", false);
        MusicUtils.getInstance().keepScreenOn = sharedPreferences.getBoolean("keepScreenOn", false);
        MusicUtils.getInstance().launchPage = sharedPreferences.getInt("launchPage", 1);
        MusicUtils.getInstance().filterNum = sharedPreferences.getInt("filterNum", 2);
        MusicUtils.getInstance().loadWebLyric = sharedPreferences.getBoolean("loadWebLyric", true);
        MusicUtils.getInstance().enableShuffle = sharedPreferences.getBoolean("enableShuffle", true);
        MusicUtils.getInstance().downloadArtist = sharedPreferences.getInt("downloadArtist", 2);
        MusicUtils.getInstance().sleepTime = sharedPreferences.getInt("sleepTime", 30);
        MusicUtils.getInstance().downloadAlbum = sharedPreferences.getInt("downloadAlbum", 0);
        MusicUtils.getInstance().checkPosition = sharedPreferences.getInt("checkPosition", 0);
        MusicUtils.getInstance().updateMusic = sharedPreferences.getInt("updateMusic", 0);
        MusicUtils.getInstance().loadlyric = sharedPreferences.getInt("loadlyric", 0);
        MusicUtils.getInstance().isPlayed = sharedPreferences.getBoolean("isPlayed", false);
        MusicUtils.getInstance().enableSwipeGesture = sharedPreferences.getBoolean("enableSwipeGesture", true);
        MusicUtils.getInstance().enableTranslateLyric = sharedPreferences.getBoolean("enableTranslateLyric", true);

        if (MusicUtils.getInstance().keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = null;
        if (am != null) {
            services = am.getRunningServices(100);
        }

        if (services != null) {
            for (ActivityManager.RunningServiceInfo info : services) {
                // 得到所有正在运行的服务的名称
                String name = info.service.getClassName();
                if (name.equals("ironbear775.com.musicplayer.Service.MusicService")) {
                    flag = 1;
                    Intent intent1 = new Intent(MusicList.this, MusicService.class);
                    bindService(intent1, conn, Service.BIND_AUTO_CREATE);
                    Log.d("bind", "bound");
                    break;
                }
            }
        }

        if (flag == 0) {
            musicService = new MusicService();
            Intent intent1 = new Intent(MusicList.this, MusicService.class);
            bindService(intent1, conn, Service.BIND_AUTO_CREATE);
            Log.d("bind", "bound");

            PlayOrPause.setImageResource(R.drawable.footplaywhite);
        } else {
            musicService = MusicService.musicService;
        }

        //toolbar
        setSupportActionBar(toolbar);
        toolbar.setTitle(getResources().getString(R.string.toolbar_title_music_player));
        toolbar.setTitleTextColor(Color.WHITE);

        initSlideDrawer();

        MusicUtils.getInstance().deleteApk();
        MusicUtils.getInstance().checkUpdate(this);
    }

    private void showFragment(int i) {
        if (slideUp != null && slideUp.isVisible())
            slideUp.hide();
        transaction = fragmentManager.beginTransaction();
        if (musicFragment != null) {
            transaction.hide(musicFragment);
        }
        if (albumFragment != null) {
            transaction.hide(albumFragment);
        }
        if (AlbumListFragment.detailFragment != null) {
            transaction.hide(AlbumListFragment.detailFragment);
        }
        if (artistFragment != null) {
            transaction.hide(artistFragment);
        }
        if (ArtistListFragment.artistDetailFragment != null
                && MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_DETAIl_PAGE) {
            Intent intent = new Intent("hide fragment on switch");
            sendBroadcast(intent);
        }
        if (ArtistListFragment.artistDetailFragment != null) {
            transaction.hide(ArtistListFragment.artistDetailFragment);
        }
        if (playlistFragment != null) {
            transaction.hide(playlistFragment);
        }
        if (PlaylistFragment.playlistDetailFragment != null) {
            transaction.hide(PlaylistFragment.playlistDetailFragment);
        }
        if (musicRecentFragment != null) {
            transaction.hide(musicRecentFragment);
        }
        if (folderFragment != null) {
            transaction.hide(folderFragment);
        }
        if (FolderFragment.folderDetailFragment != null) {
            transaction.hide(FolderFragment.folderDetailFragment);
        }

        transaction.setCustomAnimations(R.animator.fragment_slide_left_enter,
                R.animator.fragment_slide_left_exit,
                R.animator.fragment_slide_right_enter,
                R.animator.fragment_slide_right_exit);
        switch (i) {

            case 1:
                if (MusicUtils.getInstance().isArtistlistFragmentOpen) {
                    MusicUtils.getInstance().closeDownloadArtistImage();
                }
                toolbar.setTitle(R.string.toolbar_title_music_player);
                Mod = 1;
                if (musicFragment == null || flag == 1) {
                    musicFragment = new MusicListFragment();
                    transaction.add(R.id.content, musicFragment).commit();
                } else {
                    transaction.show(musicFragment).commit();
                }
                toolbar.setVisibility(View.GONE);
                getWindow().setStatusBarColor(colorPri);
                toolbar.setBackgroundColor(colorPri);
                break;
            case 2:
                toolbar.setTitle(R.string.toolbar_title_artist);
                toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
                Mod = 2;
                if (artistFragment == null || flag == 1) {
                    artistFragment = new ArtistListFragment();
                    transaction.add(R.id.content, artistFragment);
                } else {
                    transaction.show(artistFragment);
                }
                transaction.commit();

                toolbar.setVisibility(View.VISIBLE);
                getWindow().setStatusBarColor(colorPri);
                toolbar.setBackgroundColor(colorPri);
                break;
            case 3:
                if (MusicUtils.getInstance().isArtistlistFragmentOpen) {
                    MusicUtils.getInstance().closeDownloadArtistImage();
                }
                toolbar.setTitle(R.string.toolbar_title_album);
                toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
                Mod = 3;
                if (albumFragment == null || flag == 1) {
                    albumFragment = new AlbumListFragment();
                    transaction.add(R.id.content, albumFragment);
                    transaction.commit();
                } else {
                    transaction.show(albumFragment);
                    transaction.commit();
                }
                toolbar.setVisibility(View.VISIBLE);
                getWindow().setStatusBarColor(colorPri);
                toolbar.setBackgroundColor(colorPri);
                break;
            case 4:
                if (MusicUtils.getInstance().isArtistlistFragmentOpen) {
                    MusicUtils.getInstance().closeDownloadArtistImage();
                }
                toolbar.setTitle(R.string.toolbar_title_playlist);
                toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
                Mod = 4;
                if (playlistFragment == null || flag == 1) {
                    playlistFragment = new PlaylistFragment();
                    transaction.add(R.id.content, playlistFragment);
                    transaction.commit();
                } else {
                    transaction.show(playlistFragment);
                    transaction.commit();
                }
                toolbar.setVisibility(View.VISIBLE);
                getWindow().setStatusBarColor(colorPri);
                toolbar.setBackgroundColor(colorPri);
                break;
            case 5:
                if (MusicUtils.getInstance().isArtistlistFragmentOpen) {
                    MusicUtils.getInstance().closeDownloadArtistImage();
                }
                toolbar.setTitle(R.string.toolbar_title_recent_added);
                Mod = 5;
                if (musicRecentFragment == null || flag == 1) {
                    musicRecentFragment = new MusicRecentAddedFragment();
                    transaction.add(R.id.content, musicRecentFragment).commit();
                } else {
                    transaction.show(musicRecentFragment).commit();
                }
                toolbar.setVisibility(View.GONE);
                getWindow().setStatusBarColor(colorPri);
                toolbar.setBackgroundColor(colorPri);
                break;
            case 6:
                if (MusicUtils.getInstance().isArtistlistFragmentOpen) {
                    MusicUtils.getInstance().closeDownloadArtistImage();
                }
                toolbar.setTitle(R.string.toolbar_title_folder);
                toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
                Mod = 6;
                if (folderFragment == null || flag == 1) {
                    folderFragment = new FolderFragment();
                    transaction.add(R.id.content, folderFragment);
                    transaction.commit();
                } else {
                    transaction.show(folderFragment);
                    transaction.commit();
                }
                toolbar.setVisibility(View.VISIBLE);
                getWindow().setStatusBarColor(colorPri);
                toolbar.setBackgroundColor(colorPri);

                break;
        }
    }

    private void quit() {
        if (isConn)
            unbindService(conn);

        if (MusicService.mediaPlayer != null && !MusicService.mediaPlayer.isPlaying()) {

            sendBroadcast(new Intent("save data"));
            flag = 0;
        }

        unregisterReceiver(musicFinishReceiver);
    }

    private SecondarySwitchDrawerItem nightSwitch;

    private void initSlideDrawer() {
        PrimaryDrawerItem slideMusicList = new PrimaryDrawerItem()
                .withIdentifier(1)
                .withIcon(R.drawable.music)
                .withSelectedTextColorRes(R.color.material_gray_dark)
                .withName(getResources().getString(R.string.slideBar_title_music));
        PrimaryDrawerItem slideArtist = new PrimaryDrawerItem()
                .withIdentifier(2)
                .withIcon(R.drawable.artist)
                .withSelectedTextColorRes(R.color.material_gray_dark)
                .withName(getResources().getString(R.string.toolbar_title_artist));
        PrimaryDrawerItem slideAlbum = new PrimaryDrawerItem()
                .withIdentifier(3)
                .withIcon(R.drawable.album_grey)
                .withSelectedTextColorRes(R.color.material_gray_dark)
                .withName(getResources().getString(R.string.toolbar_title_album));
        PrimaryDrawerItem slidePlayList = new PrimaryDrawerItem()
                .withIdentifier(4)
                .withIcon(R.drawable.playlist)
                .withSelectedTextColorRes(R.color.material_gray_dark)
                .withName(getResources().getString(R.string.toolbar_title_playlist));
        PrimaryDrawerItem slideRecentAdded = new PrimaryDrawerItem()
                .withIdentifier(5)
                .withIcon(R.drawable.recent_added)
                .withSelectedTextColorRes(R.color.material_gray_dark)
                .withName(getResources().getString(R.string.toolbar_title_recent_added));
        PrimaryDrawerItem slideFolder = new PrimaryDrawerItem()
                .withIdentifier(6)
                .withIcon(R.drawable.folder)
                .withSelectedTextColorRes(R.color.material_gray_dark)
                .withName(getResources().getString(R.string.toolbar_title_folder));

        SecondaryDrawerItem slideSleepTimer = new SecondaryDrawerItem()
                .withIdentifier(7)
                .withIcon(R.drawable.sleeptimer)
                .withSelectedTextColorRes(R.color.material_gray_dark)
                .withName(getResources().getString(R.string.sleep_timer));

        SecondaryDrawerItem setting = new SecondaryDrawerItem()
                .withIdentifier(8)
                .withIcon(R.drawable.settings)
                .withSelectedTextColorRes(R.color.material_gray_dark)
                .withName(getResources().getString(R.string.settings));

        SecondaryDrawerItem equalizer = new SecondaryDrawerItem()
                .withIdentifier(9)
                .withIcon(R.drawable.equalizer)
                .withSelectedTextColorRes(R.color.material_gray_dark)
                .withName(getResources().getString(R.string.equalizer));

        nightSwitch = new SecondarySwitchDrawerItem()
                .withIdentifier(10)
                .withSwitchEnabled(true)
                .withChecked(isNight)
                .withOnCheckedChangeListener((drawerItem, buttonView, isChecked) -> {
                    isNight = isChecked;
                    editor.putBoolean("isNight", isChecked);
                    editor.putBoolean("isManual", true);
                    editor.commit();

                    sendBroadcast(new Intent("restart yourself"));

                })
                .withName(R.string.night_mode)
                .withIcon(R.drawable.night);

        headerView = getLayoutInflater().inflate(R.layout.account_header_layout,
                null);

        headerAlbumArt = headerView.findViewById(R.id.header_album_art);
        headerTitle = headerView.findViewById(R.id.now_playing_title);

        slideDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHeader(headerView)
                .addDrawerItems(
                        slideMusicList,
                        slideArtist,
                        slideAlbum,
                        slidePlayList,
                        slideFolder,
                        slideRecentAdded,
                        new DividerDrawerItem(),
                        slideSleepTimer,
                        nightSwitch,
                        setting,
                        equalizer
                )
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {

                    switch ((int) drawerItem.getIdentifier()) {
                        case 1:
                            showFragment(1);
                            break;
                        case 2:
                            showFragment(2);
                            break;
                        case 3:
                            showFragment(3);
                            break;
                        case 4:
                            showFragment(4);
                            break;
                        case 5:
                            showFragment(5);
                            break;
                        case 6:
                            showFragment(6);
                            break;
                        case 7:
                            SleepTimer sleepTimer = new SleepTimer(MusicList.this);
                            sleepTimer.show();
                            break;
                        case 8:
                            Intent settingIntent = new Intent(MusicList.this, Setting.class);
                            startActivity(settingIntent);
                            break;
                        case 9:
                            Intent in = new Intent(AudioEffect
                                    .ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);

                            if ((in.resolveActivity(getPackageManager()) != null)) {
                                startActivityForResult(in, 0);
                            } else {
                                if (!MusicService.isBluetoothHeadsetConnected) {
                                    Intent i = new Intent(getApplicationContext(), Equalizer.class);
                                    startActivity(i);
                                } else {
                                    Toast.makeText(MusicList.this, R.string.equalizer_cant_open,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            break;
                    }
                    return false;
                })
                .build();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        slideDrawer.setSelection(MusicUtils.getInstance().launchPage, true);
        slideDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.footPlayOrPause:

                if ((MusicListFragment.musicList.size() > 0
                        || MusicRecentAddedFragment.musicList.size() > 0
                        || ArtistListFragment.artistlist.size() > 0
                        || AlbumListFragment.albumlist.size() > 0
                        || PlaylistDetailFragment.musicList.size() > 0
                        || FolderDetailFragment.musicList.size() > 0)) {
                    if (flag == 1 && MusicService.mediaPlayer.isPlaying()) {
                        MusicService.mediaPlayer.pause();
                        PlayOrPause.setImageDrawable(playToPauseWhiteDrawable);
                        playToPauseWhiteDrawable.start();
                        //PlayOrPause.setImageResource(R.drawable.footplaywhite);
                        if (isNight || !changeMainWindow) {
                            play_pause.setImageDrawable(playToPauseWhiteDrawable);
                            playToPauseWhiteDrawable.start();
                            //play_pause.setImageResource(R.drawable.footplaywhite);
                        } else {
                            play_pause.setImageDrawable(playToPauseDrawable);
                            playToPauseDrawable.start();
                            //play_pause.setImageResource(R.drawable.footplay);
                        }
                        Intent intent = new Intent("setPlayOrPause");
                        intent.putExtra("playOrPause", R.drawable.footplay);
                        sendBroadcast(intent);
                    } else if (MusicUtils.getInstance().playPage == 0
                            && flag == 0) {
                        startMusic(MusicUtils.getInstance().pos);
                        PlayOrPause.setImageDrawable(pauseToPlayWhiteDrawable);
                        pauseToPlayWhiteDrawable.start();
                        //PlayOrPause.setImageResource(R.drawable.footpausewhite);
                        if (isNight || !changeMainWindow) {
                            play_pause.setImageDrawable(pauseToPlayWhiteDrawable);
                            pauseToPlayWhiteDrawable.start();
                            //play_pause.setImageResource(R.drawable.footpausewhite);
                        } else {
                            play_pause.setImageDrawable(pauseToPlayDrawable);
                            pauseToPlayDrawable.start();
                            //play_pause.setImageResource(R.drawable.footpause);
                        }
                        Intent intent = new Intent("setPlayOrPause");
                        intent.putExtra("playOrPause", R.drawable.footpause);
                        sendBroadcast(intent);
                    } else {
                        MusicService.mediaPlayer.start();
                        PlayOrPause.setImageDrawable(pauseToPlayWhiteDrawable);
                        pauseToPlayWhiteDrawable.start();
                        //PlayOrPause.setImageResource(R.drawable.footpausewhite);
                        if (isNight || !changeMainWindow) {
                            play_pause.setImageDrawable(pauseToPlayWhiteDrawable);
                            pauseToPlayWhiteDrawable.start();
                            //play_pause.setImageResource(R.drawable.footpausewhite);
                        } else {
                            play_pause.setImageDrawable(pauseToPlayDrawable);
                            pauseToPlayDrawable.start();
                            //play_pause.setImageResource(R.drawable.footpause);
                        }
                        Intent intent = new Intent("setPlayOrPause");
                        intent.putExtra("playOrPause", R.drawable.footpause);
                        sendBroadcast(intent);
                    }
                }
                break;
            case R.id.music_play:
                musicService.playOrPause();
                if (MusicService.mediaPlayer.isPlaying()) {
                    if (isNight || !changeMainWindow) {
                        play_pause.setImageDrawable(pauseToPlayWhiteDrawable);
                        pauseToPlayWhiteDrawable.start();
                        //play_pause.setImageResource(R.drawable.footpausewhite);
                    } else {
                        play_pause.setImageDrawable(pauseToPlayDrawable);
                        pauseToPlayDrawable.start();
                        //play_pause.setImageResource(R.drawable.footpause);
                    }
                } else {
                    if (isNight || !changeMainWindow) {
                        play_pause.setImageDrawable(playToPauseWhiteDrawable);
                        playToPauseWhiteDrawable.start();
                        //play_pause.setImageResource(R.drawable.footplaywhite);
                    } else {
                        play_pause.setImageDrawable(playToPauseDrawable);
                        playToPauseDrawable.start();
                        //play_pause.setImageResource(R.drawable.footplay);
                    }
                }
                break;
            case R.id.music_next:
                if (!MusicService.mediaPlayer.isPlaying()) {
                    if (isNight || !changeMainWindow) {
                        play_pause.setImageDrawable(pauseToPlayWhiteDrawable);
                        pauseToPlayWhiteDrawable.start();
                        //play_pause.setImageResource(R.drawable.footpausewhite);
                    } else {
                        play_pause.setImageDrawable(pauseToPlayDrawable);
                        pauseToPlayDrawable.start();
                        //play_pause.setImageResource(R.drawable.footpause);
                    }
                }
                musicService.nextMusic();
                break;
            case R.id.music_last:
                if (!MusicService.mediaPlayer.isPlaying()) {
                    if (isNight || !changeMainWindow) {
                        play_pause.setImageDrawable(pauseToPlayWhiteDrawable);
                        pauseToPlayWhiteDrawable.start();
                        //play_pause.setImageResource(R.drawable.footpausewhite);
                    } else {
                        play_pause.setImageDrawable(pauseToPlayDrawable);
                        pauseToPlayDrawable.start();
                        //play_pause.setImageResource(R.drawable.footpause);
                    }
                }
                musicService.preMusic();
                break;
            case R.id.random_play:
                MusicService.isRandom = !MusicService.isRandom;

                editor.putInt("progress", MusicService.mediaPlayer.getCurrentPosition());
                editor.putBoolean("isRandom", MusicService.isRandom);
                editor.apply();

                if (MusicService.isRandom)
                    sendBroadcast(new Intent("random play"));
                else
                    sendBroadcast(new Intent("cycle play"));
                break;
            case R.id.cycle_play:
                if (MusicService.isSingleOrCycle == 1) {
                    MusicService.isSingleOrCycle = 2;
                } else if (MusicService.isSingleOrCycle == 2) {
                    MusicService.isSingleOrCycle = 3;
                } else {
                    MusicService.isSingleOrCycle = 1;
                }

                editor.putInt("progress", MusicService.mediaPlayer.getCurrentPosition());
                editor.putInt("isSingleOrCycle", MusicService.isSingleOrCycle);
                editor.apply();

                break;
            case R.id.slide_menu:
                PopupMenu popupMenu = new PopupMenu(this, slideMenu);
                popupMenu.inflate(R.menu.menu_slide);

                ArrayList<Music> mList = new ArrayList<>();
                mList.clear();
                mList.add(MusicService.music);
                popupMenu.setOnMenuItemClickListener((MenuItem item) -> {
                    switch (item.getItemId()) {
                        case R.id.menu_add:
                            addToList(mList);
                            break;
                        case R.id.menu_delete:
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MusicList.this);
                            alertDialog.setTitle(R.string.delete_alert_title);
                            alertDialog.setMessage(mList.get(0).getTitle());
                            alertDialog.setCancelable(true);
                            alertDialog.setNegativeButton(R.string.delete_cancel, (dialog15, which) -> {

                            });
                            alertDialog.setPositiveButton(R.string.delete_confrim, (dialog14, which) -> {
                                String uri = mList.get(0).getUri();
                                getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                        MediaStore.Audio.Media.DATA + "=?",
                                        new String[]{uri});
                                File file = new File(uri);
                                if (file.isFile()) {
                                    if (file.delete()) {
                                        Intent intent = new Intent("notifyDataSetChanged");
                                        sendBroadcast(intent);

                                        Intent intentNext = new Intent("delete current music success");
                                        sendBroadcast(intentNext);

                                        Toast.makeText(getApplicationContext(),
                                                R.string.success, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                R.string.failed, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            alertDialog.show();
                            break;
                        case R.id.menu_detail:
                            DetailDialog detailDialog = new DetailDialog(
                                    MusicList.this, mList, 0);
                            detailDialog.show();
                            break;

                    }
                    return false;
                });
                popupMenu.show();
                break;
            case R.id.on_playing_list:
                OnPlayingListDialog onPlayingListDialog = new OnPlayingListDialog(MusicList.this);
                onPlayingListDialog.show();
                break;

        }

    }

    private void embedLyric(Music music) {
        String newSongTitle, newSinger;

        newSongTitle = MusicService.music.getTitle();

        if (MusicService.music.getTitle().contains("/")) {
            newSongTitle = MusicService.music.getTitle().replace("/", "_");
        }

        newSinger = MusicService.music.getArtist();

        if (MusicService.music.getArtist().contains("/")) {
            newSinger = MusicService.music.getArtist().replace("/", "_");
        }

        File file = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/MusicPlayer/lyric",
                newSongTitle + "_" + newSinger + ".lrc");

        try {
            Mp3File mp3File = new Mp3File(music.getUri());
            ID3v2 tag;
            if (mp3File.hasId3v2Tag()) {
                tag = mp3File.getId3v2Tag();
            } else {
                tag = new ID3v24Tag();
            }

            mp3File.setId3v2Tag(tag);

            if (tag.getLyrics() != null) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MusicList.this);
                builder.setTitle(R.string.already_had_lyric);
                builder.setMessage(R.string.re_embed_lyric);
                builder.setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setPositiveButton(R.string.delete_confrim, (dialog, which) -> {

                    if (file.exists()) {
                        embedLyricFormFile(file, mp3File);
                    } else {
                        if (MusicUtils.getInstance().loadWebLyric) {
                            switch (MusicUtils.getInstance().loadlyric) {
                                case 0:
                                    MusicUtils.getInstance().getWebLyric(this, MusicService.music.getTitle(),
                                            MusicService.music.getArtist(),
                                            false, true);
                                    break;
                                case 1:
                                    MusicUtils.getInstance().getWebLyricFromNetease(this, MusicService.music.getTitle(),
                                            MusicService.music.getArtist(),
                                            false, true, false);
                                    break;
                                case 2:
                                    MusicUtils.getInstance().getWebLyricFromKugou(this, MusicService.music.getTitle(),
                                            MusicService.music.getArtist(),
                                            false, true, false);
                                    break;
                            }
                        } else {
                            Toast.makeText(MusicList.this, R.string.embed_lyric_failed, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                });
                builder.show();
            } else {
                if (file.exists()) {
                    embedLyricFormFile(file, mp3File);
                } else {
                    if (MusicUtils.getInstance().loadWebLyric) {
                        switch (MusicUtils.getInstance().loadlyric) {
                            case 0:
                                MusicUtils.getInstance().getWebLyric(this, MusicService.music.getTitle(),
                                        MusicService.music.getArtist(),
                                        false, true);
                                break;
                            case 1:
                                MusicUtils.getInstance().getWebLyricFromNetease(this, MusicService.music.getTitle(),
                                        MusicService.music.getArtist(),
                                        false, true, false);
                                break;
                            case 2:
                                MusicUtils.getInstance().getWebLyricFromKugou(this, MusicService.music.getTitle(),
                                        MusicService.music.getArtist(),
                                        false, true, false);
                                break;
                        }
                    } else {
                        Toast.makeText(MusicList.this, R.string.embed_lyric_failed, Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            e.printStackTrace();
        }

    }

    public void setLyric(Context context) {
        boolean canScroll = false;

        String newSongTitle, newSinger;

        if (MusicService.music != null) {
            newSongTitle = MusicService.music.getTitle();

            if (MusicService.music.getTitle().contains("/")) {
                newSongTitle = MusicService.music.getTitle().replace("/", "_");
            }

            newSinger = MusicService.music.getArtist();

            if (MusicService.music.getArtist().contains("/")) {
                newSinger = MusicService.music.getArtist().replace("/", "_");
            }

            File file = new File(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/MusicPlayer/lyric",
                    newSongTitle + "_" + newSinger + ".lrc");

            if (MusicUtils.getInstance().downloadAlbum == 2 && MusicUtils.getInstance().haveWIFI(context)
                    || MusicUtils.getInstance().downloadAlbum == 1) {

                Bitmap bitmap = GetAlbumArt.getAlbumArtBitmap(context, MusicService.music.getAlbumArtUri(), 1);
                if (bitmap != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    Glide.with(context)
                            .load(stream.toByteArray())
                            .apply(RequestOptions
                                    .bitmapTransform(new BlurTransformation(23, 4))
                                    .placeholder(R.drawable.default_album_art)
                                    .error(R.drawable.default_album_art))
                            .transition(DrawableTransitionOptions.withCrossFade(500))
                            .into(blurBG);
                } else {
                    File blurFile = MusicUtils.getInstance().getAlbumCoverFile(MusicService.music.getArtist(), MusicService.music.getAlbum());
                    if (blurFile.exists()) {
                        Glide.with(context)
                                .load(blurFile)
                                .apply(RequestOptions
                                        .bitmapTransform(new BlurTransformation(23, 4))
                                        .placeholder(R.drawable.default_album_art)
                                        .error(R.drawable.default_album_art))
                                .transition(DrawableTransitionOptions.withCrossFade(500))
                                .into(blurBG);

                    } else {
                        MusicUtils.getInstance().getAlbumCover(this, MusicService.music.getArtist(),
                                MusicService.music.getAlbum(), MusicUtils.getInstance().FROM_MAINIMAGE);
                    }
                }
            } else {
                File blurFile = MusicUtils.getInstance().getAlbumCoverFile(MusicService.music.getArtist(), MusicService.music.getAlbum());
                if (blurFile.exists()) {
                    Glide.with(context)
                            .load(blurFile)
                            .apply(RequestOptions
                                    .bitmapTransform(new BlurTransformation(23, 4))
                                    .placeholder(R.drawable.default_album_art)
                                    .error(R.drawable.default_album_art))
                            .transition(DrawableTransitionOptions.withCrossFade(500))
                            .into(blurBG);

                } else {
                    Glide.with(context)
                            .load(MusicService.music.getAlbumArtUri())
                            .apply(RequestOptions
                                    .bitmapTransform(new BlurTransformation(23, 4))
                                    .placeholder(R.drawable.default_album_art)
                                    .error(R.drawable.default_album_art))
                            .transition(DrawableTransitionOptions.withCrossFade(500))
                            .into(blurBG);
                }
            }
            blurBG.setVisibility(View.VISIBLE);
            lyricView.loadLrc("");
            lyricView.setVisibility(View.VISIBLE);

            if (MusicService.music.getUri().contains(".mp3")) {
                try {

                    Mp3File f = new Mp3File(MusicService.music.getUri());

                    if (f.hasId3v2Tag()) {
                        String lyric = f.getId3v2Tag().getLyrics();

                        if (lyric != null) {

                            String[] array = lyric.split("\\n");
                            for (String line : array) {
                                if (line.startsWith("[")) {
                                    canScroll = true;
                                }
                            }

                            if (canScroll) {
                                lyricView.loadLrc(lyric);
                                handler1.post(runnable1);
                            } else if (MusicUtils.getInstance().enableTranslateLyric) {
                                setTranslateLyricFromFile(context, newSongTitle, newSinger);
                            }
                        } else if (MusicUtils.getInstance().enableTranslateLyric) {
                            setTranslateLyricFromFile(context, newSongTitle, newSinger);
                        } else if (file.exists()) {
                            lyricView.loadLrc(file);
                            handler1.post(runnable1);
                        } else {
                            searchLyric(context);
                        }
                    } else {
                        if (MusicUtils.getInstance().enableTranslateLyric) {
                            setTranslateLyricFromFile(context, newSongTitle, newSinger);
                        } else if (file.exists()) {
                            lyricView.loadLrc(file);
                            handler.post(runnable1);
                        } else {
                            searchLyric(context);
                        }
                    }

                } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                    e.printStackTrace();
                }
            } else if (MusicUtils.getInstance().enableTranslateLyric) {
                setTranslateLyricFromFile(context, newSongTitle, newSinger);
            } else if (file.exists()) {
                lyricView.loadLrc(file);
                handler1.post(runnable1);
            } else {
                searchLyric(context);
            }
        }
    }

    private void setTranslateLyricFromFile(Context context, String songTitle,
                                           String singer) {
        File tFile = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/MusicPlayer/lyric",
                songTitle + "_" + singer + "_translate" + ".lrc");
        if (tFile.exists()) {
            lyricView.loadLrc(tFile);
            handler.post(runnable1);
        } else {
            File file = new File(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/MusicPlayer/lyric",
                    songTitle + "_" + singer + ".lrc");
            if (file.exists()) {
                setOriginalLyricFromFile(file);
            } else {
                searchLyric(context);
            }
        }
    }

    private void setOriginalLyricFromFile(File file) {
        lyricView.loadLrc(file);
        handler1.post(runnable1);
    }

    private void embedLyricFormFile(File file, Mp3File mp3File) {
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line).append("\n");
            }
            String lyric = result.toString();

            embedLyricFormString(lyric, mp3File);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void embedLyricFormString(String lyric, Mp3File mp3File) {
        try {

            if (!"".equals(lyric)) {
                mp3File.save(mp3File.getFilename() + "_1");

                new File(mp3File.getFilename()).delete();
                new File(mp3File.getFilename() + "_1")
                        .renameTo(new File(mp3File.getFilename()));

                MP3 mp3 = new MP3(MusicService.music.getUri());
                mp3.setLyrics(lyric);
                mp3.save();

                Toast.makeText(MusicList.this, R.string.set_success, Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(MusicList.this, R.string.embed_lyric_failed, Toast.LENGTH_SHORT)
                        .show();
            }

        } catch (IOException | NotSupportedException e) {
            e.printStackTrace();
        }
    }


    private void searchLyric(Context context) {
        lyricView.setLabel(context.getResources().getString(R.string.searching_lyric));

        if (MusicUtils.getInstance().loadWebLyric) {
            switch (MusicUtils.getInstance().loadlyric) {
                case 0:
                    MusicUtils.getInstance().getWebLyric(this, MusicService.music.getTitle(),
                            MusicService.music.getArtist(),
                            true, false);
                    break;
                case 1:
                    MusicUtils.getInstance().getWebLyricFromNetease(this, MusicService.music.getTitle(),
                            MusicService.music.getArtist(),
                            true, false, false);
                    break;
                case 2:
                    MusicUtils.getInstance().getWebLyricFromKugou(this, MusicService.music.getTitle(),
                            MusicService.music.getArtist(),
                            true, false, false);
                    break;
            }
        } else
            lyricView.setLabel(context.getResources().getString(R.string.no_lyric));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //选择本地歌词
        if (requestCode == 1313 && resultCode == Activity.RESULT_OK) {

            List<Uri> files = Utils.getSelectedFilesFromResult(intent);
            for (Uri uri : files) {
                File file = Utils.getFileForUri(uri);

                if (uri.toString().contains(".lrc")) {
                    Intent i = new Intent("select_file");
                    i.putExtra("uri", file.getAbsolutePath());
                    sendBroadcast(i);
                } else {
                    Toast.makeText(this, R.string.not_lyric_file, Toast.LENGTH_LONG).show();
                }
            }
        }

        //选择本地封面
        if (requestCode == 1111 && resultCode == Activity.RESULT_OK) {
            Uri uri = intent.getData();

            if (uri != null) {
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                        "MusicPlayer/album");
                boolean isExists = true;
                boolean isMkdirs = false;
                if (!dir.exists()) {
                    isExists = false;
                    if (dir.mkdirs())
                        isMkdirs = true;
                }

                if (isExists || isMkdirs) {


                    String newAlbumTitle, newSinger;

                    newAlbumTitle = nowPlayingAlbum;

                    if (nowPlayingAlbum != null && nowPlayingAlbum.contains("/")) {
                        newAlbumTitle = nowPlayingAlbum.replace("/", "_");
                    }

                    newSinger = nowPlayingArtist;

                    if (nowPlayingArtist != null && nowPlayingArtist.contains("/")) {
                        newSinger = nowPlayingArtist.replace("/", "_");
                    }

                    try {
                        File albumFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                                "MusicPlayer/album/" + newAlbumTitle + "_" + newSinger);

                        boolean isFileExists = false;
                        boolean isDelete = false;
                        if (albumFile.exists()) {
                            isFileExists = true;
                            if (albumFile.delete())
                                isDelete = true;
                        }

                        if (!isFileExists || isDelete) {
                            BitmapFactory.Options opt = new BitmapFactory.Options();

                            opt.inPreferredConfig = Bitmap.Config.RGB_565;

                            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri),
                                    null, opt);

                            if (bitmap != null) {

                                FileOutputStream fos = null;
                                try {
                                    fos = new FileOutputStream(albumFile);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                    fos.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    try {
                                        if (fos != null) {
                                            fos.close();
                                        }
                                        if (!bitmap.isRecycled()) {
                                            bitmap.recycle();
                                        }

                                        Intent intent3 = new Intent("load album cover");
                                        intent3.putExtra("picUrl", albumFile.getAbsolutePath());
                                        sendBroadcast(intent3);

                                        Intent intent1 = new Intent("get blurBG visibility");
                                        intent1.putExtra("file", albumFile.getAbsolutePath());
                                        sendBroadcast(intent1);

                                        sendBroadcast(new Intent("notifyDataSetChanged"));

                                        if (MusicService.musicService != null) {
                                            Intent intent2 = new Intent("refresh notification");
                                            intent2.putExtra("file", albumFile.getAbsolutePath());
                                            sendBroadcast(intent2);
                                        }

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (resultCode == RESULT_OK && requestCode == 2222) {
            //再次执行上面的流程，包含权限判等
            File dir = new File(Environment.getExternalStorageDirectory(), "MusicPlayer/apk");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = (new File(dir, apkName + ".apk"));

            installApk(dir, file, apkName);
        }
    }

    public Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            if (MusicService.mediaPlayer != null && MusicService.mediaPlayer.isPlaying()) {
                lyricView.updateTime(MusicService.mediaPlayer.getCurrentPosition());
            }
            handler1.postDelayed(this, 100);
        }
    };

    private void playOrPauseMusic() {

        if (Mod != 1) {
            MusicListFragment.readMusic(this);
        }
        if (MusicUtils.getInstance().playPage == 0
                && MusicListFragment.musicList.size() >= 1
                && flag == 0) {
            MusicUtils.getInstance().playPage = 7;

            startMusic(MusicUtils.getInstance().pos);

            PlayOrPause.setImageDrawable(pauseToPlayWhiteDrawable);
            pauseToPlayWhiteDrawable.start();

            if (isNight || !changeMainWindow) {
                play_pause.setImageDrawable(pauseToPlayWhiteDrawable);
                pauseToPlayWhiteDrawable.start();
            } else {
                play_pause.setImageDrawable(pauseToPlayDrawable);
                pauseToPlayDrawable.start();
            }

            if (MusicService.isRandom) {
                if (shufflelist.size() >= 1) {
                    if (MusicUtils.getInstance().enableDefaultCover) {
                        Glide.with(MusicList.this)
                                .load(R.drawable.default_album_art)
                                .into(mainImageView);
                    } else {
                        setAlbumCoverToMainImageView(MusicList.this,
                                shufflelist.get(MusicUtils.getInstance().pos),
                                MusicUtils.getInstance().FROM_MAINIMAGE);
                    }
                }
            } else {
                if (list.size() >= 1) {
                    if (MusicUtils.getInstance().enableDefaultCover) {
                        Glide.with(MusicList.this)
                                .load(R.drawable.default_album_art)
                                .into(mainImageView);
                    } else {
                        setAlbumCoverToMainImageView(MusicList.this,
                                list.get(MusicUtils.getInstance().pos),
                                MusicUtils.getInstance().FROM_MAINIMAGE);
                    }
                }
            }
        } else if (MusicUtils.getInstance().playPage != 0
                || flag == 1) {

            MusicService.mediaPlayer.start();

            PlayOrPause.setImageDrawable(pauseToPlayWhiteDrawable);
            pauseToPlayWhiteDrawable.start();
            if (isNight || !changeMainWindow) {
                play_pause.setImageDrawable(pauseToPlayWhiteDrawable);
                pauseToPlayWhiteDrawable.start();
            } else {
                play_pause.setImageDrawable(pauseToPlayDrawable);
                pauseToPlayDrawable.start();
            }

            if (MusicUtils.getInstance().enableDefaultCover) {
                Glide.with(MusicList.this)
                        .load(R.drawable.default_album_art)
                        .into(mainImageView);
            } else {
                setAlbumCoverToMainImageView(MusicList.this,
                        MusicService.music, MusicUtils.getInstance().FROM_MAINIMAGE);
            }
        }

        sendBroadcast(new Intent("SetClickable_False"));

        sendBroadcast(new Intent("update"));

        Intent intent = new Intent("setPlayOrPause");
        intent.putExtra("playOrPause", R.drawable.footpause);
        sendBroadcast(intent);

        initMusicPlayer();

        slideUp.show();
        if (footBar.getVisibility() == View.VISIBLE) {
            footBar.setVisibility(View.GONE);
            PlayOrPause.hide(true);
        }
    }

    private void startMusic(Music music) {
        Intent serviceIntent = new Intent(MusicList.this, MusicService.class);
        serviceIntent.setAction("musiclist");

        openlist = new ArrayList<>();
        openlist.add(music);

        MusicUtils.getInstance().playPage = 7;
        serviceIntent.putExtra("from", 10);
        serviceIntent.putExtra("musicPosition", 0);
        serviceIntent.putExtra("musicProgress", 0);
        startService(serviceIntent);
    }

    //ListView和service绑定
    private void startMusic(int position) {

        Intent serviceIntent = new Intent(MusicList.this, MusicService.class);
        serviceIntent.setAction("musiclist");

        switch (MusicUtils.getInstance().playPage) {
            case 1:
                serviceIntent.putExtra("from", 1);
                break;
            case 2:
                serviceIntent.putExtra("from", 6);
                break;
            case 3:
                serviceIntent.putExtra("from", 2);
                break;
            case 4:
                serviceIntent.putExtra("from", 3);
                break;
            case 5:
                serviceIntent.putExtra("from", 4);
                break;
            case 6:
                serviceIntent.putExtra("from", 5);
                break;
            default:
                MusicUtils.getInstance().playPage = 7;
                serviceIntent.putExtra("from", 8);
        }

        serviceIntent.putExtra("musicPosition", position);
        serviceIntent.putExtra("musicProgress", progress);
        startService(serviceIntent);

    }

    private void initMusicPlayer() {

        if (musicService != null) {
            handler.post(runnable);
            setImageButtonBG();
        }
    }

    //设置按钮背景
    private void setImageButtonBG() {

        if (MusicService.isSingleOrCycle == 1) {
            if (isNight || !changeMainWindow)
                cyclePlay.setImageResource(R.drawable.cycle_true);
            else
                cyclePlay.setImageResource(R.drawable.cycle_white);
        } else if (MusicService.isSingleOrCycle == 2) {
            if (isNight || !changeMainWindow)
                cyclePlay.setImageResource(R.drawable.single_ture);
            else
                cyclePlay.setImageResource(R.drawable.single_white);
        } else {
            if (isNight || !changeMainWindow)
                cyclePlay.setImageResource(R.drawable.cycle_false);
            else
                cyclePlay.setImageResource(R.drawable.cycle_true);
        }


        if (isNight || !changeMainWindow) {
            play_pause.setColorNormal(getResources().getColor(R.color.material_gray_hard));
            play_pause.setColorPressed(getResources().getColor(R.color.material_gray_dark));
            play_pause.setColorRipple(getResources().getColor(R.color.material_gray_dark_light));
        } else {
            play_pause.setColorNormal(getResources().getColor(R.color.white));
            play_pause.setColorPressed(getResources().getColor(R.color.white_trans));
            play_pause.setColorRipple(getResources().getColor(R.color.md_white_1000));
        }

        if (!MusicService.isRandom) {
            if (isNight || !changeMainWindow)
                randomPlay.setImageResource(R.drawable.shuffle_false);
            else
                randomPlay.setImageResource(R.drawable.shuffle_true);
        } else {
            if (isNight || !changeMainWindow)
                randomPlay.setImageResource(R.drawable.shuffle_true);
            else
                randomPlay.setImageResource(R.drawable.shuffle_white);
        }
        if (MusicUtils.getInstance().loadWebLyric) {
            onPlayingListButton.setVisibility(View.VISIBLE);
            if (isNight || !changeMainWindow)
                onPlayingListButton.setImageResource(R.drawable.playlist);
            else
                onPlayingListButton.setImageResource(R.drawable.on_playing_list_white);
        }
        if (isNight || !changeMainWindow) {
            next.setImageResource(R.drawable.next);
            last.setImageResource(R.drawable.previous);
            slideMenu.setImageResource(R.drawable.list_menu);
        } else {
            next.setImageResource(R.drawable.next_white);
            last.setImageResource(R.drawable.previous_white);
            slideMenu.setImageResource(R.drawable.list_menu_white);
        }
    }

    private final Handler handler = new Handler();
    private final Handler handler1 = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if (slideUp.isVisible()) {
                if (MusicService.mediaPlayer != null) {
                    musicProgress.setMax(MusicService.mediaPlayer.getDuration());
                    musicProgress.setProgress(MusicService.mediaPlayer.getCurrentPosition());
                    musicProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            MusicService.mediaPlayer.seekTo(seekBar.getProgress());
                            lyricView.onDrag(seekBar.getProgress());
                        }
                    });

                    current.setText(time.format(MusicService.mediaPlayer.getCurrentPosition()));
                    if (MusicService.mediaPlayer.isPlaying()) {
                        String albumText = getResources().getString(R.string.album) + MusicService.music.getAlbum();
                        String artistText = getResources().getString(R.string.artist) + MusicService.music.getArtist();
                        album.setText(albumText);
                        title.setText(MusicService.music.getTitle());
                        artist.setText(artistText);
                        setImageButtonBG();
                        duration.setText(time.format(MusicService.mediaPlayer.getDuration()));
                    } else {
                        setImageButtonBG();
                    }

                }
            }

            handler.postDelayed(runnable, 1000);

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void shufflePlay() {

        switch (Mod) {
            case 1:
                if (MusicListFragment.musicList.size() >= 1) {
                    MusicListFragment.pos = MusicUtils.getInstance().createRandom(MusicListFragment.musicList);
                    progress = 0;

                    MusicUtils.getInstance().playPage = 1;

                    MusicUtils.getInstance().shufflePlay(this, MusicListFragment.musicList, 1);
                }
                break;
            case 2:
                if (ArtistListFragment.artistDetailFragment != null && ArtistDetailFragment.musicList.size() >= 1) {
                    ArtistDetailFragment.pos = MusicUtils.getInstance().createRandom(ArtistDetailFragment.musicList);
                    progress = 0;

                    MusicUtils.getInstance().playPage = 3;

                    MusicUtils.getInstance().shufflePlay(this, ArtistDetailFragment.musicList, 2);
                }
                break;
            case 3:
                if (AlbumListFragment.detailFragment != null && AlbumDetailFragment.musicList.size() >= 1) {
                    AlbumDetailFragment.pos = MusicUtils.getInstance().createRandom(AlbumDetailFragment.musicList);
                    progress = 0;

                    MusicUtils.getInstance().playPage = 4;

                    MusicUtils.getInstance().shufflePlay(this, AlbumDetailFragment.musicList, 3);

                }
                break;
            case 4:
                if (PlaylistFragment.playlistDetailFragment != null && PlaylistDetailFragment.musicList.size() >= 1) {
                    PlaylistDetailFragment.pos = MusicUtils.getInstance().createRandom(PlaylistDetailFragment.musicList);
                    progress = 0;

                    MusicUtils.getInstance().playPage = 5;

                    MusicUtils.getInstance().shufflePlay(this, PlaylistDetailFragment.musicList, 4);
                }
                break;
            case 5:
                if (MusicRecentAddedFragment.musicList.size() >= 1) {
                    MusicRecentAddedFragment.pos = MusicUtils.getInstance().createRandom(MusicRecentAddedFragment.musicList);
                    progress = 0;

                    MusicUtils.getInstance().playPage = 2;

                    MusicUtils.getInstance().shufflePlay(this, MusicRecentAddedFragment.musicList, 6);

                }
                break;
            case 6:
                if (FolderFragment.folderDetailFragment != null && FolderDetailFragment.musicList.size() >= 1) {
                    FolderDetailFragment.pos = MusicUtils.getInstance().createRandom(FolderDetailFragment.musicList);
                    progress = 0;

                    MusicUtils.getInstance().playPage = 6;

                    MusicUtils.getInstance().shufflePlay(this, FolderDetailFragment.musicList, 5);
                }
                break;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.getItem(0);
        if (Mod == 2 && ArtistListFragment.artistDetailFragment == null
                || Mod == 3 && AlbumListFragment.detailFragment == null
                || Mod == 4
                || Mod == 6 && FolderFragment.folderDetailFragment == null) {
            item.setEnabled(false);
            item.setVisible(false);
        } else {
            item.setEnabled(true);
            item.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_shuffle:
                shufflePlay();
                break;
            case R.id.sleepTimer:
                SleepTimer sleepTimer = new SleepTimer(MusicList.this);
                sleepTimer.show();
                return false;
            case R.id.search_song:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.donate:
                try {
                    String qrcode = URLEncoder.encode(MusicUtils.getInstance().MY_ALIPAY_QRCODE, "utf-8");
                    final String alipayqr = "alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode="
                            + qrcode;
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(alipayqr + "%3F_s%3Dweb-other&_t=" + System.currentTimeMillis())));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    Toast.makeText(MusicList.this, R.string.check_alipay_installed,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.contact_me:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                            "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D"
                                    + "2bqJtkjtHw4QfEvPJQpFnHFT_vkUpEN4")));
                } catch (Exception e) {
                    Toast.makeText(MusicList.this, R.string.check_qq_installed,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.rate:
                try {
                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent jumpIntent = new Intent("android.intent.action.VIEW", uri);
                    jumpIntent.setPackage("com.coolapk.market");//指定应用市场
                    jumpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(jumpIntent);
                } catch (Exception e) {
                    Toast.makeText(MusicList.this, R.string.check_qq_installed,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        quit();
    }


    //按下返回键不销毁activity
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CLEAR) {
            moveTaskToBack(true);
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (slideUp.isVisible()) {
                slideUp.hide();
                Intent intent = new Intent("SetClickable_True");
                sendBroadcast(intent);

                MusicUtils.getInstance().cancelNetCall();
                lyricView.setVisibility(View.GONE);
                blurBG.setVisibility(View.GONE);

                if (footBar.getVisibility() == View.GONE) {
                    footBar.setVisibility(View.VISIBLE);
                    PlayOrPause.show(true);
                }

            } else {
                if (actionMode != null) {

                    Intent in = new Intent("notifyDataSetChanged");
                    MusicUtils.getInstance().isSelectAll = false;

                    switch (Mod) {
                        case 1:
                            MusicListFragment.positionSet.clear();
                            sendBroadcast(in);
                            getWindow().setStatusBarColor(colorPri);
                            toolbar.setBackgroundColor(colorPri);
                            break;
                        case 2:
                            if (MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_DETAIl_PAGE) {
                                AlbumDetailFragment.positionSet.clear();
                                sendBroadcast(in);
                                getWindow().setStatusBarColor(AlbumDetailFragment.playColor);
                                toolbar.setBackgroundColor(0);
                            } else if ((MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_PAGE
                                    && ArtistDetailFragment.CLICK_SONGLIST)) {
                                ArtistDetailFragment.positionSet.clear();
                                sendBroadcast(in);
                                getWindow().setStatusBarColor(ArtistDetailFragment.playColor);
                                toolbar.setBackgroundColor(0);
                            } else if ((MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_PAGE
                                    && ArtistDetailFragment.CLICK_ALBUMLIST)) {
                                ArtistDetailFragment.albumPositionSet.clear();
                                sendBroadcast(in);
                                getWindow().setStatusBarColor(ArtistDetailFragment.playColor);
                                toolbar.setBackgroundColor(0);
                            } else if (artistFragment != null) {
                                ArtistListFragment.positionSet.clear();
                                toolbar.setBackgroundColor(colorPri);
                                getWindow().setStatusBarColor(colorPri);
                                sendBroadcast(in);
                            }
                            break;
                        case 3:
                            if (MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ALBUM_PAGE) {
                                AlbumDetailFragment.positionSet.clear();
                                sendBroadcast(in);
                                getWindow().setStatusBarColor(AlbumDetailFragment.playColor);
                                toolbar.setBackgroundColor(0);
                            } else if (albumFragment != null) {
                                AlbumListFragment.positionSet.clear();
                                toolbar.setBackgroundColor(colorPri);
                                getWindow().setStatusBarColor(colorPri);
                                sendBroadcast(in);
                            }
                            break;
                        case 5:
                            MusicRecentAddedFragment.positionSet.clear();
                            sendBroadcast(in);
                            getWindow().setStatusBarColor(colorPri);
                            toolbar.setBackgroundColor(colorPri);
                            break;
                        case 6:
                            if (FolderFragment.folderDetailFragment != null) {

                                FolderDetailFragment.positionSet.clear();
                                getWindow().setStatusBarColor(colorPri);
                                toolbar.setBackgroundColor(colorPri);
                                sendBroadcast(in);
                            }
                            break;
                    }
                } else {
                    switch (Mod) {
                        case 1:
                            moveTaskToBack(true);
                            break;
                        case 2:
                            if (ArtistListFragment.artistDetailFragment != null
                                    && MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_DETAIl_PAGE) {

                                Intent intent = new Intent("hide albumDetailFragment");
                                sendBroadcast(intent);

                                getWindow().setStatusBarColor(ArtistDetailFragment.playColor);
                                toolbar.setBackgroundColor(0);
                                MusicUtils.getInstance().fromWhere = MusicUtils.getInstance().FROM_ARTIST_PAGE;

                            } else if (ArtistListFragment.artistDetailFragment != null
                                    && MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_PAGE) {

                                getWindow().setStatusBarColor(colorPri);
                                toolbar.setBackgroundColor(colorPri);
                                transaction = fragmentManager.beginTransaction();
                                if (ArtistListFragment.artistDetailFragment != null)
                                    transaction.remove(ArtistListFragment.artistDetailFragment);
                                transaction.setCustomAnimations(
                                        R.animator.fragment_slide_right_enter,
                                        R.animator.fragment_slide_right_exit,
                                        R.animator.fragment_slide_left_enter,
                                        R.animator.fragment_slide_left_exit
                                );
                                transaction.show(artistFragment);
                                transaction.commit();
                                ArtistListFragment.artistDetailFragment = null;
                                toolbar.setVisibility(View.VISIBLE);
                                toolbar.setTitle(R.string.toolbar_title_artist);
                                MusicUtils.getInstance().fromWhere = MusicUtils.getInstance().CLEAR;

                            } else if (artistFragment != null) {
                                moveTaskToBack(true);
                            }
                            break;
                        case 3:
                            if (AlbumListFragment.detailFragment != null) {
                                getWindow().setStatusBarColor(colorPri);
                                toolbar.setBackgroundColor(colorPri);
                                transaction = fragmentManager.beginTransaction();

                                if (AlbumListFragment.detailFragment != null)
                                    transaction.hide(AlbumListFragment.detailFragment);
                                transaction.setCustomAnimations(
                                        R.animator.fragment_slide_right_enter,
                                        R.animator.fragment_slide_right_exit,
                                        R.animator.fragment_slide_left_enter,
                                        R.animator.fragment_slide_left_exit

                                );
                                transaction.show(albumFragment);
                                transaction.commit();
                                AlbumListFragment.detailFragment = null;

                                toolbar.setVisibility(View.VISIBLE);
                                toolbar.setTitle(R.string.toolbar_title_album);

                            } else if (albumFragment != null) {
                                moveTaskToBack(true);
                            }
                            MusicUtils.getInstance().fromWhere = MusicUtils.getInstance().CLEAR;
                            break;
                        case 4:
                            if (PlaylistFragment.playlistDetailFragment != null) {
                                if (PlaylistDetailFragment.isChange) {
                                    new Thread(() -> {
                                        PlaylistDbHelper dbHelper = new PlaylistDbHelper(getApplicationContext(),
                                                PlaylistDetailFragment.name + ".db", "");
                                        SQLiteDatabase database = dbHelper.getWritableDatabase();
                                        database.delete(PlaylistDetailFragment.name, null, null);
                                        for (int i = 0; i < PlaylistDetailFragment.musicList.size(); i++) {
                                            ContentValues values = new ContentValues();

                                            values.put("title", PlaylistDetailFragment.musicList.get(i).getTitle());
                                            values.put("artist", PlaylistDetailFragment.musicList.get(i).getArtist());
                                            values.put("albumArtUri", PlaylistDetailFragment.musicList.get(i).getAlbumArtUri());
                                            values.put("album", PlaylistDetailFragment.musicList.get(i).getAlbum());
                                            values.put("uri", PlaylistDetailFragment.musicList.get(i).getUri());
                                            database.insert(PlaylistDetailFragment.name, null, values);
                                        }
                                        database.close();
                                        PlaylistDetailFragment.isChange = false;
                                    }).start();
                                }
                                transaction = fragmentManager.beginTransaction();
                                if (PlaylistFragment.playlistDetailFragment != null)
                                    transaction.remove(PlaylistFragment.playlistDetailFragment);
                                transaction.setCustomAnimations(
                                        R.animator.fragment_slide_right_enter,
                                        R.animator.fragment_slide_right_exit,
                                        R.animator.fragment_slide_left_enter,
                                        R.animator.fragment_slide_left_exit
                                );
                                transaction.show(playlistFragment);
                                transaction.commit();
                                toolbar.setVisibility(View.VISIBLE);
                                toolbar.setTitle(R.string.toolbar_title_playlist);
                                PlaylistFragment.playlistDetailFragment = null;
                            } else if (playlistFragment != null) {
                                moveTaskToBack(true);
                            }
                            break;
                        case 5:
                            moveTaskToBack(true);
                            break;
                        case 6:
                            if (FolderFragment.folderDetailFragment != null) {
                                getWindow().setStatusBarColor(colorPri);
                                toolbar.setBackgroundColor(colorPri);
                                transaction = fragmentManager.beginTransaction();
                                if (FolderFragment.folderDetailFragment != null)
                                    transaction.hide(FolderFragment.folderDetailFragment);
                                transaction.setCustomAnimations(
                                        R.animator.fragment_slide_right_enter,
                                        R.animator.fragment_slide_right_exit,
                                        R.animator.fragment_slide_left_enter,
                                        R.animator.fragment_slide_left_exit
                                );
                                transaction.show(folderFragment);
                                transaction.commit();
                                FolderFragment.folderDetailFragment = null;

                                toolbar.setVisibility(View.VISIBLE);
                                toolbar.setTitle(R.string.toolbar_title_folder);
                            } else if (folderFragment != null) {
                                moveTaskToBack(true);
                            }
                            break;
                        default:
                            moveTaskToBack(true);
                    }
                }

            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (actionMode == null) {
            actionMode = mode;

            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_action_mode, menu);
            actionModeIsActive = true;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        backWasPressedInActionMode = actionModeIsActive && event.getKeyCode() == KeyEvent.KEYCODE_BACK;
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        getWindow().setStatusBarColor(colorPri);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;

        actionModeIsActive = false;
        if (!backWasPressedInActionMode)
            onActionModeBackPressed();
        backWasPressedInActionMode = false;

        sendBroadcast(new Intent("notifyDataSetChanged"));
        sendBroadcast(new Intent("notifyAdapterIsClickable"));
    }

    private void onActionModeBackPressed() {

        Intent in = new Intent("notifyDataSetChanged");

        if (MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_DETAIl_PAGE) {

            AlbumDetailFragment.positionSet.clear();
            sendBroadcast(in);
            getWindow().setStatusBarColor(AlbumDetailFragment.playColor);
            toolbar.setBackgroundColor(0);

        } else {

            switch (Mod) {
                case 1:
                    MusicListFragment.positionSet.clear();
                    sendBroadcast(in);
                    getWindow().setStatusBarColor(colorPri);
                    toolbar.setBackgroundColor(colorPri);
                    break;
                case 2:
                    if (MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_DETAIl_PAGE) {
                        AlbumDetailFragment.positionSet.clear();
                        sendBroadcast(in);
                        getWindow().setStatusBarColor(AlbumDetailFragment.playColor);
                        toolbar.setBackgroundColor(0);
                    } else if ((MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_PAGE
                            && ArtistDetailFragment.CLICK_SONGLIST)) {
                        ArtistDetailFragment.positionSet.clear();
                        sendBroadcast(in);
                        getWindow().setStatusBarColor(ArtistDetailFragment.playColor);
                        toolbar.setBackgroundColor(0);
                    } else if ((MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_PAGE
                            && ArtistDetailFragment.CLICK_ALBUMLIST)) {
                        ArtistDetailFragment.albumPositionSet.clear();
                        sendBroadcast(in);
                        getWindow().setStatusBarColor(ArtistDetailFragment.playColor);
                        toolbar.setBackgroundColor(0);
                    } else if (artistFragment != null) {
                        ArtistListFragment.positionSet.clear();
                        sendBroadcast(in);
                        getWindow().setStatusBarColor(colorPri);
                        toolbar.setBackgroundColor(0);
                    }
                    break;
                case 3:
                    if (AlbumListFragment.detailFragment == null && albumFragment != null) {
                        AlbumListFragment.positionSet.clear();
                        sendBroadcast(in);
                        getWindow().setStatusBarColor(colorPri);
                        toolbar.setBackgroundColor(colorPri);
                    } else if (AlbumListFragment.detailFragment != null) {
                        AlbumDetailFragment.positionSet.clear();
                        getWindow().setStatusBarColor(AlbumDetailFragment.playColor);
                        toolbar.setBackgroundColor(0);
                        sendBroadcast(in);
                    }
                    break;
                case 5:
                    MusicRecentAddedFragment.positionSet.clear();
                    sendBroadcast(in);
                    getWindow().setStatusBarColor(colorPri);
                    toolbar.setBackgroundColor(colorPri);
                    break;
                case 6:
                    if (FolderFragment.folderDetailFragment != null) {
                        FolderDetailFragment.positionSet.clear();
                        getWindow().setStatusBarColor(colorPri);
                        toolbar.setBackgroundColor(colorPri);
                        sendBroadcast(in);
                    }
                    break;
            }
        }
        MusicList.listPositionSet.clear();
        musicList.clear();
    }


    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        Intent intent = new Intent("notifyDataSetChanged");
        switch (item.getItemId()) {
            case R.id.select_all:
                listPositionSet.clear();
                switch (Mod) {
                    case 1:
                        MusicUtils.getInstance().selectAll(this, listPositionSet, MusicListFragment.musicList);
                        isArtist = false;
                        break;
                    case 2:
                        if (MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_DETAIl_PAGE) {

                            MusicUtils.getInstance().selectAll(this, listPositionSet, AlbumDetailFragment.musicList);
                            isArtist = false;

                        } else if ((MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_PAGE
                                && ArtistDetailFragment.CLICK_SONGLIST)) {

                            MusicUtils.getInstance().selectAll(this, listPositionSet, ArtistDetailFragment.musicList);
                            isArtist = false;

                        } else if ((MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_PAGE
                                && ArtistDetailFragment.CLICK_ALBUMLIST)) {
                            MusicUtils.getInstance().selectAll(this, listPositionSet, ArtistDetailFragment.albumList);
                            isAlbum = true;

                        } else {

                            MusicUtils.getInstance().selectAll(this, listPositionSet, ArtistListFragment.artistlist);
                            isArtist = true;
                        }

                        break;
                    case 3:
                        if (AlbumListFragment.detailFragment != null) {
                            MusicUtils.getInstance().selectAll(this, listPositionSet, AlbumDetailFragment.musicList);
                            isArtist = false;
                        } else {
                            MusicUtils.getInstance().selectAll(this, listPositionSet, AlbumListFragment.albumlist);
                            isAlbum = true;
                        }
                        break;
                    case 5:
                        MusicUtils.getInstance().selectAll(this, listPositionSet, MusicRecentAddedFragment.musicList);
                        isArtist = false;
                        break;
                    case 6:
                        if (FolderFragment.folderDetailFragment != null) {
                            MusicUtils.getInstance().selectAll(this, listPositionSet, FolderDetailFragment.musicList);
                            isArtist = false;
                        }
                        break;
                }
                sendBroadcast(intent);
                break;
            case R.id.add_to_list:
                switch (Mod) {
                    case 1:
                        if (!MusicUtils.getInstance().isSelectAll) {
                            listPositionSet = MusicListFragment.positionSet;
                        }
                        musicList = MusicListFragment.musicList;
                        isArtist = false;
                        break;
                    case 2:
                        if (MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_DETAIl_PAGE) {

                            if (!MusicUtils.getInstance().isSelectAll) {
                                listPositionSet = AlbumDetailFragment.positionSet;
                            }
                            musicList = AlbumDetailFragment.musicList;
                            isArtist = false;

                        } else if ((MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_PAGE
                                && ArtistDetailFragment.CLICK_SONGLIST)) {

                            if (!MusicUtils.getInstance().isSelectAll) {
                                listPositionSet = ArtistDetailFragment.positionSet;
                            }
                            musicList = ArtistDetailFragment.musicList;
                            isArtist = false;

                        } else if ((MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ARTIST_PAGE
                                && ArtistDetailFragment.CLICK_ALBUMLIST)) {

                            if (!MusicUtils.getInstance().isSelectAll) {
                                listPositionSet = ArtistDetailFragment.albumPositionSet;
                            }
                            musicList = ArtistDetailFragment.albumList;
                            isAlbum = true;
                            artistInAlbum = ArtistDetailFragment.artist;

                        } else {

                            if (!MusicUtils.getInstance().isSelectAll) {
                                listPositionSet = ArtistListFragment.positionSet;
                            }
                            musicList = ArtistListFragment.artistlist;
                            isArtist = true;

                        }

                        break;
                    case 3:
                        if (AlbumListFragment.detailFragment != null
                                && MusicUtils.getInstance().fromWhere == MusicUtils.getInstance().FROM_ALBUM_PAGE) {

                            if (!MusicUtils.getInstance().isSelectAll) {
                                listPositionSet = AlbumDetailFragment.positionSet;
                            }
                            musicList = AlbumDetailFragment.musicList;
                            isArtist = false;

                        } else {
                            if (!MusicUtils.getInstance().isSelectAll) {
                                listPositionSet = AlbumListFragment.positionSet;
                            }
                            musicList = AlbumListFragment.albumlist;
                            isAlbum = true;
                        }
                        break;
                    case 5:
                        if (!MusicUtils.getInstance().isSelectAll) {
                            listPositionSet = MusicRecentAddedFragment.positionSet;
                        }
                        musicList = MusicRecentAddedFragment.musicList;
                        isArtist = false;
                        break;
                    case 6:
                        if (FolderFragment.folderDetailFragment != null) {
                            if (!MusicUtils.getInstance().isSelectAll) {
                                listPositionSet = FolderDetailFragment.positionSet;
                            }
                            musicList = FolderDetailFragment.musicList;
                            isArtist = false;
                        }
                        break;
                }

                PlaylistDialog dialog = new PlaylistDialog(this, listPositionSet, musicList);
                dialog.show();
                break;
        }
        return false;
    }

    public static class positionBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Mod == 1) {
                MusicListFragment.pos = intent.getIntExtra("position", 0);
            } else if (Mod == 5) {
                MusicRecentAddedFragment.pos = intent.getIntExtra("position", 0);
            }
        }

    }

    class MusicFinishReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case "open file":
                        openMusicFromFile(intent);
                        Log.d("TAG", "onReceive: open file");
                        break;
                    case "Music play to the end":
                        if (MusicUtils.getInstance().enableDefaultCover) {
                            Glide.with(MusicList.this)
                                    .load(R.drawable.default_album_art)
                                    .into(mainImageView);
                        } else {
                            setAlbumCoverToMainImageView(MusicList.this,
                                    MusicService.music, MusicUtils.getInstance().FROM_MAINIMAGE);
                        }

                        Log.d("Music play to the end", "Music play to the end");

                        break;
                    case "ActionModeChanged":
                        actionMode = startSupportActionMode(MusicList.this);
                        if (intent.getStringExtra("from") != null)
                            fromWhere = intent.getStringExtra("from");
                        Log.d("ActionModeChanged", "ActionModeChanged");
                        break;
                    case "FLAG RESET TO 0":
                        flag = 0;
                        PlayOrPause.setImageDrawable(pauseToPlayWhiteDrawable);
                        pauseToPlayWhiteDrawable.start();
                        Log.d("FLAG RESET TO 0", "FLAG RESET TO 0");
                        break;
                    case "search_play":

                        footTitle.setText(MusicService.music.getTitle());
                        footArtist.setText(MusicService.music.getArtist());

                        PlayOrPause.setImageDrawable(pauseToPlayWhiteDrawable);
                        pauseToPlayWhiteDrawable.start();

                        MusicUtils.getInstance().setAlbumCoverToFootAndHeader(MusicList.this, MusicService.music,
                                MusicUtils.getInstance().FROM_FOOTBAR);

                        handler.post(runnable);
                        Log.d("search_play", "search_play");
                        break;
                    case "PlAYORPAUSE":
                        if (!MusicService.mediaPlayer.isPlaying()) {
                            if (MusicService.music == null)
                                startMusic(MusicListFragment.pos);
                        }
                        Log.d("PlAYORPAUSE", "PlAYORPAUSE musiclist");
                        break;
                    case "select_file":

                        File file = new File(intent.getStringExtra("uri"));

                        lyricView.loadLrc(file);
                        handler1.post(runnable1);

                        int bytesum = 0;
                        int byteread;

                        String newSongTitle, newSinger;
                        newSongTitle = MusicService.music.getTitle();

                        if (MusicService.music.getTitle().contains("/")) {
                            newSongTitle = MusicService.music.getTitle()
                                    .replace("/", "_");
                        }

                        newSinger = MusicService.music.getArtist();

                        if (MusicService.music.getArtist().contains("/")) {
                            newSinger = MusicService.music.getArtist()
                                    .replace("/", "_");
                        }

                        String newPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/MusicPlayer/lyric" + "/" + newSongTitle + "_" + newSinger + "_translate" + ".lrc";
                        String oldPath = file.getPath();
                        File oldfile = new File(oldPath);
                        File file1 = new File(newPath);
                        if (oldfile.exists()) {

                            if (file1.exists())
                                file1.delete();

                            InputStream inStream;
                            try {
                                inStream = new FileInputStream(oldPath);
                                FileOutputStream fs = new FileOutputStream(newPath);
                                byte[] buffer = new byte[1444];
                                while ((byteread = inStream.read(buffer)) != -1) {
                                    bytesum += byteread; //字节数 文件大小
                                    System.out.println(bytesum);
                                    fs.write(buffer, 0, byteread);
                                }
                                inStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        Log.d("select_file", "select_file");
                        break;
                    case "update_cover":
                        setAlbumCoverToMainImageView(MusicList.this,
                                MusicService.music, MusicUtils.getInstance().FROM_MAINIMAGE);
                        Log.d("update_cover", "update_cover");
                        break;
                    case "enableDefaultCover":
                        Glide.with(MusicList.this)
                                .load(R.drawable.default_album_art)
                                .into(mainImageView);
                        Log.d("enableDefaultCover", "enableDefaultCover");
                        break;
                    case "embed lyric":
                        String lyric = intent.getStringExtra("lyric");

                        try {
                            Mp3File mp3File = new Mp3File(MusicService.music.getUri());
                            ID3v2 tag;
                            if (mp3File.hasId3v2Tag()) {
                                tag = mp3File.getId3v2Tag();
                            } else {
                                tag = new ID3v24Tag();
                            }

                            mp3File.setId3v2Tag(tag);

                            if (tag.getLyrics() != null) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MusicList.this);
                                builder.setTitle(R.string.already_had_lyric);
                                builder.setMessage(R.string.re_embed_lyric);
                                builder.setNegativeButton(R.string.delete_cancel, (dialog, which) -> {
                                });
                                builder.setPositiveButton(R.string.delete_confrim, (dialog, which) -> embedLyricFormString(lyric, mp3File));
                                builder.show();
                            } else {
                                embedLyricFormString(lyric, mp3File);
                            }
                        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                            e.printStackTrace();
                        }

                        Log.d("embed lyric", "embed lyric");
                        break;
                    case "update lyric":
                        if (lyricView.isShown())
                            lyricView.loadLrc(intent.getStringExtra("lyric"));
                        handler1.post(runnable1);
                        Log.d("update lyric", "update lyric");
                        break;
                    case "show lyric":
                        if (lyricView != null)
                            lyricView.loadLrc(intent.getStringExtra("lyric"));
                        handler1.post(runnable1);
                        Log.d("show lyric", "show lyric");
                        break;
                    case "load lyric failed":
                        if (lyricView != null)
                            lyricView.loadLrc("");
                        lyricView.setLabel(getResources().getString(R.string.no_lyric));
                        Log.d("load lyric failed", "load lyric failed");
                        break;
                    case "update lyric failed":
                        if (lyricView.isShown())
                            lyricView.loadLrc("");
                        lyricView.setLabel(getResources().getString(R.string.no_lyric));
                        Log.d("update lyric failed", "update lyric failed");
                        break;
                    case "load footbar cover":
                        String picUri = intent.getStringExtra("picUrl");
                        MusicUtils.getInstance().loadImageUseGlide(context, footAlbumArt,
                                picUri);
                        MusicUtils.getInstance().loadImageUseGlide(context, headerAlbumArt,
                                picUri);
                        Log.d("load footbar cover", "load footbar cover");
                        break;
                    case "load album cover":
                        String uri = intent.getStringExtra("picUrl");
                        MusicUtils.getInstance().loadImageUseGlide(context, mainImageView, uri);
                        MusicUtils.getInstance().loadImageUseGlide(context, footAlbumArt, uri);
                        MusicUtils.getInstance().loadImageUseGlide(context, headerAlbumArt, uri);

                        Log.d("load album cover", "load album cover");

                        break;
                    case "load album cover failed":
                        Glide.with(context)
                                .load(R.drawable.default_album_art)
                                .into(mainImageView);
                        Log.d("load album cover failed", "load album cover failed");

                        Glide.with(context)
                                .load(R.drawable.default_album_art)
                                .apply(new RequestOptions()
                                        .centerCrop())
                                .into(footAlbumArt);
                        Glide.with(context)
                                .load(R.drawable.default_album_art)
                                .apply(RequestOptions.centerCropTransform())
                                .into(headerAlbumArt);
                        break;
                    case "load footbar cover failed":
                        Glide.with(context)
                                .load(R.drawable.default_album_art)
                                .apply(new RequestOptions()
                                        .centerCrop())
                                .into(footAlbumArt);
                        Glide.with(context)
                                .load(R.drawable.default_album_art)
                                .apply(RequestOptions.centerCropTransform())
                                .into(headerAlbumArt);
                        break;
                    case "show blurBG":
                        if (blurBG.getVisibility() == View.VISIBLE) {
                            Glide.with(context)
                                    .load(intent.getStringExtra("picUrl"))
                                    .apply(RequestOptions.bitmapTransform(
                                            new BlurTransformation(23, 4))
                                            .placeholder(R.drawable.default_album_art)
                                            .error(R.drawable.default_album_art))
                                    .transition(DrawableTransitionOptions.withCrossFade(500))
                                    .into(blurBG);
                            Log.d("show blurBG", "show blurBG");
                        }
                        break;
                    case "restart yourself":
                        reCreateView();
                        break;
                    case "set toolbar gone":
                        toolbar.setVisibility(View.GONE);
                        break;
                    case "set toolbar text":
                        toolbar.setBackgroundColor(colorPri);
                        toolbar.setVisibility(View.VISIBLE);
                        toolbar.setTitle(intent
                                .getIntExtra("title", R.string.toolbar_title_music_player));
                        break;
                    case "set toolbar clear":
                        toolbar.setBackgroundColor(colorPri);
                        break;
                    case "set toolbar color":
                        toolbar.setBackgroundColor(
                                intent.getIntExtra("color", colorPri));
                        break;
                    case "set PlayOrPause":
                        AnimatedVectorDrawable drawable = (AnimatedVectorDrawable)
                                getResources().getDrawable(intent.getIntExtra(
                                        "PlayOrPause", R.drawable.play_to_pause_white_anim));
                        PlayOrPause.setImageDrawable(drawable);
                        drawable.start();

                        AnimatedVectorDrawable drawable1 = (AnimatedVectorDrawable)
                                getResources().getDrawable(intent.getIntExtra(
                                        "play_Pause", R.drawable.play_to_pause_white_anim));
                        play_pause.setImageDrawable(drawable1);
                        drawable1.start();

                        break;
                    case "set footBar":
                        String s1 = intent.getStringExtra("footTitle");
                        String s2 = intent.getStringExtra("footArtist");
                        String s = s1 + "-" + s2;
                        footTitle.setText(s1);
                        footArtist.setText(s2);
                        headerTitle.setText(s);
                        break;
                    case "load image with uri":
                        MusicUtils.getInstance().loadImageUseGlide(
                                getApplicationContext(), footAlbumArt,
                                intent.getStringExtra("uri"));
                        MusicUtils.getInstance().loadImageUseGlide(
                                getApplicationContext(), headerAlbumArt,
                                intent.getStringExtra("uri"));
                        break;
                    case "show snackBar":
                        Snackbar.make(PlayOrPause, intent.getIntExtra("text id", R.string.unknown_error), Snackbar.LENGTH_SHORT)
                                .setDuration(1000)
                                .show();
                        break;
                    case "open drawer":
                        slideDrawer.openDrawer();
                        break;
                    case "set lyricView visibility":
                        lyricView.setVisibility(intent.getIntExtra("visibility", 0));
                        break;
                    case "set blurBG visibility":
                        blurBG.setVisibility(intent.getIntExtra("visibility", 0));
                        break;
                    case "set lyric from service":
                        if (lyricView.getVisibility() == View.VISIBLE)
                            setLyric(getApplicationContext());
                        break;
                    case "get blurBG visibility":
                        if (blurBG.getVisibility() == View.VISIBLE) {
                            Intent intent2 = new Intent("show blurBG");
                            intent2.putExtra("file", intent.getStringExtra("file"));
                            sendBroadcast(intent2);
                        }
                        break;
                    case "keep screen on":
                        if (MusicUtils.getInstance().keepScreenOn)
                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        else
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        break;
                    case "show footbar":
                        SharedPreferences.Editor editor = context.getSharedPreferences("data", MODE_PRIVATE).edit();
                        editor.putBoolean("isPlayed", true);
                        editor.apply();
                        if (footBar.getVisibility() == View.GONE) {
                            footBar.setVisibility(View.VISIBLE);
                            PlayOrPause.show(true);
                        }
                        break;
                    case "hide footbar":
                        break;
                    case "new version":
                        String versionName = intent.getStringExtra("versionName");
                        String downloadUrl = intent.getStringExtra("downloadUrl");
                        String intro = intent.getStringExtra("intro");

                        AlertDialog.Builder updateDailog = new AlertDialog.Builder(MusicList.this)
                                .setTitle(R.string.find_new_version)
                                .setMessage(getResources().getString(R.string.version) + versionName
                                        + "\n" + getResources().getString(R.string.new_version_title)
                                        + "\n" + intro
                                        + "\n\n" + getResources().getString(R.string.download_now))
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        apkName = "music_player" + versionName;
                                        downloadApk(downloadUrl, apkName);
                                    }
                                })
                                .setNegativeButton(R.string.no, null);

                        updateDailog.create().show();
                        break;
                    case Intent.ACTION_TIME_CHANGED:
                    case Intent.ACTION_TIME_TICK:
                        Calendar calendar = Calendar.getInstance();
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);

                        if (!isNight && hour >= 22 || (0 <= hour && hour < 7)) {
                            isNight = true;
                            nightSwitch.withChecked(true);
                            sendBroadcast(new Intent("restart yourself"));
                            Log.e("TAG", "restart yourself to night mode");

                        }
                        if (isNight && hour >= 7 && hour < 22) {
                            isNight = false;
                            nightSwitch.withChecked(false);
                            sendBroadcast(new Intent("restart yourself"));
                            Log.e("TAG", "restart yourself to day mode");

                        }
                        break;
                }
            }
        }
    }

    private String apkName;
    private boolean isDownloadCancel = false;

    //下载安装包
    private void downloadApk(String uri, String filename) {
        File dir = new File(Environment.getExternalStorageDirectory(), "MusicPlayer/apk");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = (new File(dir, filename + ".apk"));

        ProgressDialog dialog = new ProgressDialog(MusicList.this);
        dialog.setMessage(getResources().getString(R.string.downloading));
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.delete_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isDownloadCancel = true;
                MusicUtils.getInstance().cancelNetCall();
                if (file.exists())
                    file.delete();
            }
        });

        dialog.show();

        if (file.exists()) {
            installApk(dir, file, filename);
            dialog.dismiss();
        } else {

            MusicUtils.getInstance().downloadApk(uri, filename, new MusicUtils.OnDownloadListener() {
                @Override
                public void onDownloadSuccess() {
                    dialog.dismiss();
                    if (!isDownloadCancel) {
                        installApk(dir, file, filename);
                    }
                }

                @Override
                public void onDownloading(int progress) {

                }

                @Override
                public void onDownloadFailed() {
                    dialog.dismiss();
                    Looper.prepare();
                    Toast.makeText(MusicList.this, R.string.download_failed, Toast.LENGTH_SHORT).show();
                    Looper.loop();

                    if (file.exists())
                        file.delete();
                }
            });
        }
    }

    //安装apk
    private void installApk(File dir, File file, String filename) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (getPackageManager().canRequestPackageInstalls()) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri apkUri = FileProvider.getUriForFile(MusicList.this,
                        "ironbear775.com.musicplayer.provider", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                startActivity(intent);
            } else {
                Intent intent1 = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                startActivityForResult(intent1, 2222);

            }
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri apkUri = FileProvider.getUriForFile(MusicList.this,
                    "ironbear775.com.musicplayer.provider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(new File(dir, filename + ".apk")),
                    "application/vnd.android.package-archive");
        }
        startActivity(intent);
    }

    /**
     * 切换应用主题
     */
    private void reCreateView() {

        setTheme(MusicList.this, sharedPreferences);
        try {
            Resources.Theme theme = getTheme();
            TypedValue appBgValue = new TypedValue();
            TypedValue mainTextColorValue = new TypedValue();
            TypedValue seekBarNextColorValue = new TypedValue();
            TypedValue seekBarPassColorValue = new TypedValue();
            TypedValue slideMainColorValue = new TypedValue();
            TypedValue slideSubColorValue = new TypedValue();
            TypedValue slideTopColorValue = new TypedValue();
            TypedValue slideSubTextColorValue = new TypedValue();
            TypedValue slideMainTextColorValue = new TypedValue();
            TypedValue colorAccentValue = new TypedValue();
            TypedValue colorPrimaryValue = new TypedValue();
            TypedValue colorPrimaryDarkValue = new TypedValue();

            theme.resolveAttribute(R.attr.appBg, appBgValue, true);
            theme.resolveAttribute(R.attr.mainTextColor, mainTextColorValue, true);
            theme.resolveAttribute(R.attr.seekBarNextColor, seekBarNextColorValue, true);
            theme.resolveAttribute(R.attr.seekBarPassColor, seekBarPassColorValue, true);
            theme.resolveAttribute(R.attr.slideMainColor, slideMainColorValue, true);
            theme.resolveAttribute(R.attr.slideSubColor, slideSubColorValue, true);
            theme.resolveAttribute(R.attr.slideTopColor, slideTopColorValue, true);
            theme.resolveAttribute(R.attr.slideSubTextColor, slideSubTextColorValue, true);
            theme.resolveAttribute(R.attr.slideMainTextColor, slideMainTextColorValue, true);
            theme.resolveAttribute(R.attr.colorPrimary, colorPrimaryValue, true);
            theme.resolveAttribute(R.attr.colorPrimaryDark, colorPrimaryDarkValue, true);
            theme.resolveAttribute(R.attr.colorAccent, colorAccentValue, true);
            Resources resources = getResources();
            int appBg = ResourcesCompat.getColor(resources,
                    appBgValue.resourceId, null);
            int mainTextColor = ResourcesCompat.getColor(resources,
                    mainTextColorValue.resourceId, null);
            int seekBarNextColor = ResourcesCompat.getColor(resources,
                    seekBarNextColorValue.resourceId, null);
            int seekBarPassColor = ResourcesCompat.getColor(resources,
                    seekBarPassColorValue.resourceId, null);
            int slideMainColor = ResourcesCompat.getColor(resources,
                    slideMainColorValue.resourceId, null);
            int slideSubColor = ResourcesCompat.getColor(resources,
                    slideSubColorValue.resourceId, null);
            int slideSubTextColor = ResourcesCompat.getColor(resources,
                    slideSubTextColorValue.resourceId, null);
            int slideMainTextColor = ResourcesCompat.getColor(resources,
                    slideMainTextColorValue.resourceId, null);
            int colorPrimary = ResourcesCompat.getColor(resources,
                    colorPrimaryValue.resourceId, null);
            int colorPrimaryDark = ResourcesCompat.getColor(resources,
                    colorPrimaryDarkValue.resourceId, null);
            int colorAccent = ResourcesCompat.getColor(resources,
                    colorAccentValue.resourceId, null);

            getWindow().setStatusBarColor(colorPrimary);

            colorPri = colorPrimary;
            toolbar.setBackgroundColor(colorPrimary);
            footBar.setBackgroundColor(appBg);

            footTitle.setTextColor(mainTextColor);
            footArtist.setTextColor(mainTextColor);
            headerTitle.setTextColor(colorAccent);
            headerView.setBackgroundColor(colorPrimary);

            PlayOrPause.setColorNormal(colorPrimary);
            PlayOrPause.setColorPressed(colorPrimaryDark);
            PlayOrPause.setColorRipple(colorAccent);

            if (MusicService.mediaPlayer.isPlaying()) {
                if (isNight || !changeMainWindow)
                    play_pause.setImageResource(R.drawable.footpausewhite);
                else
                    play_pause.setImageResource(R.drawable.footpause);
            } else {
                if (isNight || !changeMainWindow)
                    play_pause.setImageResource(R.drawable.footplaywhite);
                else
                    play_pause.setImageResource(R.drawable.footplay);
            }

            title.setTextColor(slideMainTextColor);
            artist.setTextColor(slideSubTextColor);
            album.setTextColor(slideSubTextColor);

            Drawable drawable = getDrawable(R.drawable.top_round_bg);
            RelativeLayout uiTitle = findViewById(R.id.ui_title);
            uiTitle.setBackground(drawable);

            RelativeLayout middle = findViewById(R.id.middle);
            middle.setBackgroundColor(slideSubColor);

            RelativeLayout music_control = findViewById(R.id.music_control);
            music_control.setBackgroundColor(slideMainColor);

            RelativeLayout current_lo = findViewById(R.id.current_lo);
            current_lo.setBackgroundColor(seekBarPassColor);

            RelativeLayout duration_lo = findViewById(R.id.duration_lo);
            duration_lo.setBackgroundColor(seekBarNextColor);

            musicProgress.getProgressDrawable().setColorFilter(
                    seekBarNextColor, PorterDuff.Mode.SRC);

            if (isNight)
                slideDrawer.getSlider().setBackgroundColor(
                        Color.parseColor("#303030"));
            else
                slideDrawer.getSlider().setBackgroundColor(
                        getResources().getColor(R.color.lightBg)
                );

            List<IDrawerItem> items = slideDrawer.getDrawerItems();
            for (int i = 0; i < 6; i++) {
                PrimaryDrawerItem item = (PrimaryDrawerItem) items.get(i);
                if (isNight) {
                    item.withSelectedColor(Color.parseColor("#202020"));
                    item.withTextColor(Color.WHITE);
                } else {
                    item.withSelectedColor(Color.parseColor("#e8e8e8"));
                    item.withTextColor(Color.parseColor("#383838"));
                }
                slideDrawer.updateItem(item);
            }
            for (int i = 7; i < 11; i++) {
                if (i != 8) {
                    SecondaryDrawerItem item = (SecondaryDrawerItem) items.get(i);
                    if (isNight) {
                        item.withSelectedColor(Color.parseColor("#202020"));
                        item.withTextColor(Color.parseColor("#c1c1c1"));
                    } else {
                        item.withSelectedColor(Color.parseColor("#e8e8e8"));
                        item.withTextColor(Color.parseColor("#737373"));
                    }
                    slideDrawer.updateItem(item);
                } else {
                    SecondarySwitchDrawerItem item = (SecondarySwitchDrawerItem) items.get(8);
                    if (isNight) {
                        item.withSelectedColor(Color.parseColor("#202020"));
                        item.withTextColor(Color.parseColor("#c1c1c1"));
                    } else {
                        item.withSelectedColor(Color.parseColor("#e8e8e8"));
                        item.withTextColor(Color.parseColor("#737373"));
                    }
                    slideDrawer.updateItem(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static IntentFilter musicFinishFilter() {
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("open file");
        intentfilter.addAction("Music play to the end");
        intentfilter.addAction("ActionModeChanged");
        intentfilter.addAction("FLAG RESET TO 0");
        intentfilter.addAction("search_play");
        intentfilter.addAction("PlAYORPAUSE");
        intentfilter.addAction("select_file");
        intentfilter.addAction("update_cover");
        intentfilter.addAction("enableDefaultCover");
        intentfilter.addAction("embed lyric");
        intentfilter.addAction("show lyric");
        intentfilter.addAction("load lyric failed");
        intentfilter.addAction("update lyric failed");
        intentfilter.addAction("update lyric");
        intentfilter.addAction("load album cover");
        intentfilter.addAction("load album cover failed");
        intentfilter.addAction("show blurBG");
        intentfilter.addAction("load footbar cover");
        intentfilter.addAction("restart yourself");
        intentfilter.addAction("set toolbar gone");
        intentfilter.addAction("set toolbar text");
        intentfilter.addAction("set toolbar color");
        intentfilter.addAction("set toolbar clear");
        intentfilter.addAction("set footBar");
        intentfilter.addAction("set PlayOrPause");
        intentfilter.addAction("load image with uri");
        intentfilter.addAction("show snackBar");
        intentfilter.addAction("open drawer");
        intentfilter.addAction("set lyricView visibility");
        intentfilter.addAction("set blurBG visibility");
        intentfilter.addAction("set lyric from service");
        intentfilter.addAction("get blurBG visibility");
        intentfilter.addAction("show fragment");
        intentfilter.addAction("hide fragment");
        intentfilter.addAction("keep screen on");
        intentfilter.addAction("show footbar");
        intentfilter.addAction("hide footbar");
        intentfilter.addAction("new version");
        intentfilter.addAction(Intent.ACTION_TIME_TICK);
        intentfilter.addAction(Intent.ACTION_TIME_CHANGED);
        return intentfilter;
    }

    /**
     * 将歌曲专辑封面显示在ImageView中
     *
     * @param context context
     * @param music   当前需要显示封面的歌曲
     * @param from    歌曲封面需要显示的ImageView（footbar、mainImage、service）
     */
    private void setAlbumCoverToMainImageView(Context context, Music music, int from) {
        if (music != null) {
            if (MusicUtils.getInstance().downloadAlbum == 2 && MusicUtils.getInstance().haveWIFI(context)
                    || MusicUtils.getInstance().downloadAlbum == 1) {

                Bitmap bitmap = GetAlbumArt.getAlbumArtBitmap(context, music.getAlbumArtUri(), 1);
                if (bitmap != null) {
                    MusicUtils.getInstance().loadImageUseGlide(context, mainImageView, bitmap);
                } else {
                    File file = MusicUtils.getInstance().getAlbumCoverFile(music.getArtist(), music.getAlbum());
                    if (file.exists()) {
                        MusicUtils.getInstance().loadImageUseGlide(context, mainImageView, file);
                    } else {
                        MusicUtils.getInstance().getAlbumCover(this, music.getArtist(),
                                music.getAlbum(), from);
                    }
                }
            } else {
                File file = MusicUtils.getInstance().getAlbumCoverFile(music.getArtist(), music.getAlbum());
                if (file.exists())
                    MusicUtils.getInstance().loadImageUseGlide(context, mainImageView, file);
                else
                    MusicUtils.getInstance().loadImageUseGlide(context, mainImageView,
                            music.getAlbumArtUri());
            }
        } else {
            MusicUtils.getInstance().loadImageUseGlide(context, mainImageView,
                    BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art));
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (getScreenHeight(this) * 1.0 / getScreenWidth(this) > 1.9) {
            updateSizeInfo();
        }
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(dm);
        }
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(dm);
        }
        return dm.heightPixels;
    }

    /**
     * 动态调整播放页面布局大小
     */
    private void updateSizeInfo() {
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            int actionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data, getResources().getDisplayMetrics());

            RelativeLayout top_blank = findViewById(R.id.top_blank);
            RelativeLayout ui_title = findViewById(R.id.ui_title);
            RelativeLayout middle = findViewById(R.id.middle);
            RelativeLayout music_control = findViewById(R.id.music_control);
            RelativeLayout music_track = findViewById(R.id.music_track);
            LinearLayout middle_layout = findViewById(R.id.middle_layout);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) top_blank.getLayoutParams();
            params.height = actionBarHeight - actionBarHeight / 5;
            top_blank.setLayoutParams(params);

            int size = middle_layout.getHeight() + ui_title.getHeight();

            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) ui_title.getLayoutParams();
            params1.height = size * 4 / 19;
            ui_title.setLayoutParams(params1);

            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) middle.getLayoutParams();
            params2.height = size * 3 / 19;
            middle.setLayoutParams(params2);

            LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams) music_control.getLayoutParams();
            params3.height = size * 10 / 19;
            music_control.setLayoutParams(params3);

            LinearLayout.LayoutParams params4 = (LinearLayout.LayoutParams) music_track.getLayoutParams();
            params4.height = size * 2 / 19;
            music_track.setLayoutParams(params4);

        }
    }

    private boolean isLongClick;

    @SuppressLint("ClickableViewAccessibility")
    private void findView() {
        slideView = findViewById(R.id.slideview);
        slideMenu = findViewById(R.id.slide_menu);
        footBar = findViewById(R.id.footBar);
        play_pause = findViewById(R.id.music_play);
        next = findViewById(R.id.music_next);
        last = findViewById(R.id.music_last);
        title = findViewById(R.id.title);
        artist = findViewById(R.id.artist);
        album = findViewById(R.id.album);
        mainImageView = findViewById(R.id.album_art);
        footTitle = findViewById(R.id.footTitle);
        footArtist = findViewById(R.id.footArtist);
        footAlbumArt = findViewById(R.id.footAlbumArt);
        PlayOrPause = findViewById(R.id.footPlayOrPause);
        duration = findViewById(R.id.duration);
        current = findViewById(R.id.current);
        musicProgress = findViewById(R.id.music_progress);
        randomPlay = findViewById(R.id.random_play);
        cyclePlay = findViewById(R.id.cycle_play);
        lyricView = findViewById(R.id.lyric_view);
        onPlayingListButton = findViewById(R.id.on_playing_list);
        blurBG = findViewById(R.id.album_art_blur);
        toolbar = findViewById(R.id.toolbar);
        playToPauseDrawable = (AnimatedVectorDrawable)
                getResources().getDrawable(R.drawable.play_to_pause_anim);

        pauseToPlayDrawable = (AnimatedVectorDrawable)
                getResources().getDrawable(R.drawable.pause_to_play_anim);

        playToPauseWhiteDrawable = (AnimatedVectorDrawable)
                getResources().getDrawable(R.drawable.play_to_pause_white_anim);
        pauseToPlayWhiteDrawable = (AnimatedVectorDrawable)
                getResources().getDrawable(R.drawable.pause_to_play_white_anim);

        PlayOrPause.setOnClickListener(this);
        slideMenu.setOnClickListener(this);
        play_pause.setOnClickListener(this);
        next.setOnClickListener(this);
        last.setOnClickListener(this);
        randomPlay.setOnClickListener(this);
        cyclePlay.setOnClickListener(this);
        onPlayingListButton.setOnClickListener(this);
        fragmentManager = getFragmentManager();


        mainImageView.setOnViewLongClickListener(new SquareImageView.OnLongClickListener() {
            @Override
            public void OnLongClick() {
                if (lyricView.getVisibility() == View.GONE) {
                    setMainPopupDialog(mainImageView.getContext());
                } else if (lyricView.getVisibility() == View.VISIBLE) {
                    setLyricPopupDialog(mainImageView.getContext());
                }
                isLongClick = true;
            }
        });

        mainImageView.setOnTouchListener((v, event) -> {

            slideUp.onTouch(slideView, event);

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                xDown = event.getX();
                yDown = event.getY();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {// 松开处理
                if (!isLongClick) {
                    xUp = event.getX();
                    yUp = event.getY();

                    int swipe = 175;
                    if (density == 3.0) {
                        swipe = 175;
                    } else if (density == 4.0) {
                        swipe = 250;
                    } else if (density == 2.0) {
                        swipe = 150;
                    }


                    if (MusicUtils.getInstance().enableSwipeGesture) {
                        if ((xUp - xDown) > swipe) {
                            if (MusicUtils.getInstance().playPage != 0
                                    || MusicService.mediaPlayer.isPlaying()
                                    || flag == 1)
                                musicService.preMusic();
                        } else if ((xUp - xDown) < -swipe) {
                            if (MusicUtils.getInstance().playPage != 0
                                    || MusicService.mediaPlayer.isPlaying()
                                    || flag == 1)
                                musicService.nextMusic();
                        }
                    }

                    if (5 >= Math.abs(xDown - xUp) && 5 >= Math.abs(yDown - yUp)) {
                        if (lyricView.getVisibility() == View.VISIBLE) {
                            lyricView.setVisibility(View.GONE);
                            blurBG.setVisibility(View.GONE);
                        } else
                            setLyric(getApplicationContext());
                    }
                }

                isLongClick = false;
            }
            return true;
        });

        footBar.setOnTouchListener((View v, MotionEvent event) -> {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                xDown = event.getX();
                yDown = event.getY();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {// 松开处理

                xUp = event.getX();
                yUp = event.getY();

                int swipe = 175;
                if (density == 3.0) {
                    swipe = 175;
                } else if (density == 4.0) {
                    swipe = 250;
                } else if (density == 2.0) {
                    swipe = 150;
                }

                if ((xUp - xDown) > swipe && 100 >= Math.abs(yDown - yUp)) {
                    if (MusicUtils.getInstance().playPage != 0
                            || MusicService.mediaPlayer.isPlaying()
                            || flag == 1)
                        musicService.preMusic();
                } else if ((xUp - xDown) < -swipe && 100 >= Math.abs(yDown - yUp)) {
                    if (MusicUtils.getInstance().playPage != 0
                            || MusicService.mediaPlayer.isPlaying()
                            || flag == 1)
                        musicService.nextMusic();
                } else if (5 >= Math.abs(xDown - xUp) && 5 >= Math.abs(yDown - yUp)
                        || 50 <= yDown - yUp) {
                    playOrPauseMusic();
                }
            }
            return true;
        });
    }


    private void setMainPopupDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.option);
        builder.setItems(R.array.menu_main, (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    ArrayList<Music> musicArrayList = new ArrayList<>();
                    musicArrayList.add(MusicService.music);
                    addToList(musicArrayList);
                    break;
                case 1:
                    if (MusicService.music.getUri().contains(".mp3")
                            || MusicService.music.getUri().contains(".MP3")) {
                        Intent intent = new Intent(getApplicationContext(),
                                TagEditActivity.class);
                        intent.putExtra("music",
                                (Parcelable) MusicService.music);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                R.string.open_failed, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:
                    Intent intent1 = new Intent();
                    nowPlayingAlbum = MusicService.music.getAlbum();
                    nowPlayingArtist = MusicService.music.getArtist();
                    intent1.setType("image/*");
                    intent1.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent1, 1111);
                    break;
            }
        });
        builder.show();
    }

    private void addToList(ArrayList<Music> arrayList) {
        Set<Integer> listPositionSet = new HashSet<>();
        listPositionSet.add(0);
        PlaylistDialog dialog = new PlaylistDialog(
                MusicList.this, listPositionSet, arrayList);
        dialog.show();
    }

    private void setLyricPopupDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.option);
        builder.setItems(R.array.menu_lyric, (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    Intent intent = new Intent(getApplicationContext(), FilePickerActivity.class);

                    intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                    intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                    intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

                    intent.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                    startActivityForResult(intent, 1313);
                    break;
                case 1:
                    String[] update = new String[]{getResources().getString(R.string.netease),
                            getResources().getString(R.string.kugou)};
                    AlertDialog.Builder lyricBuilder;
                    if (!isNight)
                        lyricBuilder = new AlertDialog.Builder(PlayOrPause.getContext(), R.style.MaterialThemeDialog);
                    else
                        lyricBuilder = new AlertDialog.Builder(PlayOrPause.getContext());

                    lyricBuilder.setTitle(R.string.update_lyric);
                    lyricBuilder.setItems(update, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                if (lyricView != null)
                                    MusicUtils.getInstance().updateLyricFromNetease(MusicList.this,
                                            MusicService.music.getTitle(),
                                            MusicService.music.getArtist(),
                                            true);
                                else
                                    MusicUtils.getInstance().updateLyricFromNetease(MusicList.this,
                                            MusicService.music.getTitle(),
                                            MusicService.music.getArtist(),
                                            false);
                            } else if (which == 1) {
                                if (lyricView != null)
                                    MusicUtils.getInstance().updateLyricFromKugou(MusicList.this,
                                            MusicService.music.getTitle(),
                                            MusicService.music.getArtist(),
                                            true);
                                else
                                    MusicUtils.getInstance().updateLyricFromKugou(MusicList.this,
                                            MusicService.music.getTitle(),
                                            MusicService.music.getArtist(),
                                            false);
                            }
                            dialog.dismiss();
                        }
                    });
                    lyricBuilder.show();
                    break;
                case 2:
                    EditText et = new EditText(getApplicationContext());
                    et.setHint(R.string.input_song_id);
                    et.setTextColor(getResources().getColor(R.color.black));
                    new AlertDialog.Builder(PlayOrPause.getContext())
                            .setView(et)
                            .setTitle(R.string.accurate_update)
                            .setPositiveButton(R.string.delete_confrim, (dialog1, which) -> {

                                if (et.getText() != null && !et.getText().toString().equals("")) {
                                    if (lyricView != null) {
                                        MusicUtils.getInstance().getLyricFromNeteaseById(this,
                                                et.getText().toString(),
                                                MusicService.music.getTitle(),
                                                MusicService.music.getArtist(),
                                                true, false, false);
                                    } else {
                                        MusicUtils.getInstance().getLyricFromNeteaseById(this,
                                                et.getText().toString(),
                                                MusicService.music.getTitle(),
                                                MusicService.music.getArtist(),
                                                false, false, false);
                                    }
                                }
                            })
                            .create().show();
                    break;
                case 3:
                    if (MusicService.music.getUri().contains(".mp3")
                            || MusicService.music.getUri().contains(".MP3")) {

                        embedLyric(MusicService.music);
                    } else {
                        Toast.makeText(MusicList.this,
                                R.string.open_failed, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        });
        builder.show();
    }
}




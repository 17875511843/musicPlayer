package ironbear775.com.musicplayer.Activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.beaglebuddy.mp3.MP3;
import com.bumptech.glide.Glide;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondarySwitchDrawerItem;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    public static ArrayList<Music> shufflelist;
    public static Set<Integer> listPositionSet = new HashSet<>();
    public static String fromWhere;
    public static String artistInALbum;
    public static int count = 0;
    public static boolean first = true;
    public static int statusBarColor;

    private TextView footTitle;
    private TextView footArtist;
    private ImageView footAlbumArt;
    private FloatingActionButton PlayOrPause;
    private FloatingActionButton play_pause;
    private AccountHeader accountHeader;
    private ImageView slideMenu;
    private SlideUp slideUp;
    private MusicService musicService;
    private Toolbar toolbar;
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
    private MusicUtils musicUtils;
    private LrcView lyricView;
    private ImageView lyricButton;
    private SquareImageView blurBG;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private MusicFinishReceiver musicFinishReceiver;
    private final SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    private int progress = 0;
    private float xUp, yDown, yUp, xDown;
    private long pressTime, releaseTime;
    private float density;
    private String nowPlayingAlbum, nowPlayingArtist;
    private boolean backWasPressedInActionMode = false;
    private boolean actionModeIsActive = false;
    private ImageView last, next;

    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MusicBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @SuppressLint("SdCardPath")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.music_list_layout);

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

        MusicUtils.isFlyme = MusicUtils.isFlyme(this);

        musicFinishReceiver = new MusicFinishReceiver();

        registerReceiver(musicFinishReceiver, musicFinishFilter());

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        ComponentName audioButton = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());

        if (audioManager != null) {
            audioManager.registerMediaButtonEventReceiver(audioButton);
        }

        findView();

        first = sharedPreferences.getBoolean("firstTime", true);

        if (!first) {
            list = MusicUtils.getArray(this);
            shufflelist = MusicUtils.getShuffleArray(this);
        }

        init();

        toolbar.inflateMenu(R.menu.menu_short);

    }

    private void init() {

        statusBarColor = getWindow().getStatusBarColor();

        musicUtils = new MusicUtils(this);

        MusicUtils.pos = sharedPreferences.getInt("position", 0);
        progress = sharedPreferences.getInt("progress", 0);
        MusicService.isRandom = sharedPreferences.getBoolean("isRandom", false);
        MusicService.isSingleOrCycle = sharedPreferences.getInt("isSingleOrCycle", 1);
        flag = sharedPreferences.getInt("flag", 0);
        AlbumDetailFragment.count = sharedPreferences.getInt("albumCount", 0);
        ArtistDetailFragment.count = sharedPreferences.getInt("artistCount", 0);
        MusicListFragment.count = sharedPreferences.getInt("musicCount", 0);
        MusicRecentAddedFragment.count = sharedPreferences.getInt("musicRecentCount", 0);
        PlaylistDetailFragment.count = sharedPreferences.getInt("playlistCount", 0);
        FolderDetailFragment.count = sharedPreferences.getInt("folderlistcount", 0);
        MusicUtils.enableDefaultCover = sharedPreferences.getBoolean("enableDefaultCover", false);
        MusicUtils.enableColorNotification = sharedPreferences.getBoolean("enableColorNotification", false);
        MusicUtils.enableEqualizer = sharedPreferences.getBoolean("enableEqualizer", false);
        MusicUtils.useOldStyleNotification = sharedPreferences.getBoolean("useOldStyleNotification", false);
        MusicUtils.keepScreenOn = sharedPreferences.getBoolean("keepScreenOn", false);
        MusicUtils.launchPage = sharedPreferences.getInt("launchPage", 1);
        MusicUtils.filterNum = sharedPreferences.getInt("filterNum", 2);
        MusicUtils.loadWebLyric = sharedPreferences.getBoolean("loadWebLyric", true);
        MusicUtils.enableShuffle = sharedPreferences.getBoolean("enableShuffle", true);
        MusicUtils.downloadArtist = sharedPreferences.getInt("downloadArtist", 2);
        MusicUtils.sleepTime = sharedPreferences.getInt("sleepTime", 30);
        MusicUtils.downloadAlbum = sharedPreferences.getInt("downloadAlbum", 0);
        MusicUtils.checkPosition = sharedPreferences.getInt("checkPosition", 0);
        MusicUtils.updateMusic = sharedPreferences.getInt("updateMusic", 0);
        MusicUtils.loadlyric = sharedPreferences.getInt("loadlyric", 0);
        MusicUtils.enableSwipeGesture = sharedPreferences.getBoolean("enableSwipeGesture", true);
        MusicUtils.enableTranslateLyric = sharedPreferences.getBoolean("enableTranslateLyric", true);

        if (MusicUtils.keepScreenOn) {
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
            PlayOrPause.setImageResource(R.drawable.footplaywhite);
        } else {
            musicService = MusicService.musicService;
        }

        //toolbar
        setSupportActionBar(toolbar);
        toolbar.setTitle(getResources().getString(R.string.toolbar_title_music_player));
        toolbar.setTitleTextColor(Color.WHITE);

        initSlideDrawer();

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

                            musicUtils.cancelNetCall();
                            lyricView.setVisibility(View.GONE);
                            blurBG.setVisibility(View.GONE);
                            footBar.setVisibility(View.VISIBLE);
                            PlayOrPause.show();
                        }
                    }
                })
                .build();
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
                && MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_DETAIl_PAGE) {
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
                if (ArtistListFragment.count == 1) {
                    MusicUtils.closeDownloadArtistImage();
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
                getWindow().setStatusBarColor(0);
                toolbar.setBackgroundColor(0);
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
                getWindow().setStatusBarColor(0);
                toolbar.setBackgroundColor(0);
                break;
            case 3:
                if (ArtistListFragment.count == 1) {
                    MusicUtils.closeDownloadArtistImage();
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
                getWindow().setStatusBarColor(0);
                toolbar.setBackgroundColor(0);
                break;
            case 4:
                if (ArtistListFragment.count == 1) {
                    MusicUtils.closeDownloadArtistImage();
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
                getWindow().setStatusBarColor(0);
                toolbar.setBackgroundColor(0);
                break;
            case 5:
                if (ArtistListFragment.count == 1) {
                    MusicUtils.closeDownloadArtistImage();
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
                getWindow().setStatusBarColor(0);
                toolbar.setBackgroundColor(0);
                break;
            case 6:
                if (ArtistListFragment.count == 1) {
                    MusicUtils.closeDownloadArtistImage();
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
                getWindow().setStatusBarColor(0);
                toolbar.setBackgroundColor(0);

                break;
        }
    }

    private void quit() {

        unregisterReceiver(musicFinishReceiver);

        if (MusicService.mediaPlayer != null && !MusicService.mediaPlayer.isPlaying()) {
            MusicService.mediaPlayer.pause();
            Intent intent = new Intent(MusicList.this, MusicService.class);
            stopService(intent);
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(1);
            }

            editor.putInt("progress", MusicService.mediaPlayer.getCurrentPosition());
            editor.putInt("flag", 0);
            editor.apply();
            editor.commit();
            finish();
            ActivityCollector.finishAll();
            System.exit(0);
        }
    }

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

        SecondarySwitchDrawerItem nightSwitch = new SecondarySwitchDrawerItem()
                .withIdentifier(10)
                .withSwitchEnabled(true)
                .withChecked(BaseActivity.isNight)
                .withDescription(R.string.night_mode_test)
                .withOnCheckedChangeListener((drawerItem, buttonView, isChecked) -> {
                    BaseActivity.isNight = isChecked;
                    editor.putBoolean("isNight", isChecked);
                    editor.putBoolean("isManual", true);
                    editor.commit();

                    sendBroadcast(new Intent("restart yourself"));

                })
                .withName(R.string.night_mode)
                .withIcon(R.drawable.night);

        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withOnAccountHeaderSelectionViewClickListener((view, profile) -> {
                    slideDrawer.closeDrawer();
                    playOrPauseMusic();
                    return false;
                })
                .build();

        slideDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
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
                                Intent i = new Intent(getApplicationContext(), Equalizer.class);
                                startActivity(i);
                            }
                            break;
                    }
                    return false;
                })
                .build();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        slideDrawer.setSelection(MusicUtils.launchPage, true);
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
                    if (MusicService.mediaPlayer.isPlaying()) {
                        MusicService.mediaPlayer.pause();
                        PlayOrPause.setImageResource(R.drawable.footplaywhite);
                        if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                            play_pause.setImageResource(R.drawable.footplaywhite);
                        else
                            play_pause.setImageResource(R.drawable.footplay);

                        Intent intent = new Intent("setPlayOrPause");
                        intent.putExtra("playOrPause", R.drawable.footplay);
                        sendBroadcast(intent);
                    } else if (MusicListFragment.count == 0
                            && AlbumDetailFragment.count == 0
                            && MusicRecentAddedFragment.count == 0
                            && ArtistDetailFragment.count == 0
                            && PlaylistDetailFragment.count == 0
                            && FolderDetailFragment.count == 0
                            && count == 0
                            && !MusicService.mediaPlayer.isPlaying()
                            && flag == 0) {
                        startMusic(MusicUtils.pos);
                        PlayOrPause.setImageResource(R.drawable.footpausewhite);
                        if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                            play_pause.setImageResource(R.drawable.footpausewhite);
                        else
                            play_pause.setImageResource(R.drawable.footpause);

                        Intent intent = new Intent("setPlayOrPause");
                        intent.putExtra("playOrPause", R.drawable.footpause);
                        sendBroadcast(intent);
                    } else {
                        MusicService.mediaPlayer.start();
                        PlayOrPause.setImageResource(R.drawable.footpausewhite);
                        if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                            play_pause.setImageResource(R.drawable.footpausewhite);
                        else
                            play_pause.setImageResource(R.drawable.footpause);
                        Intent intent = new Intent("setPlayOrPause");
                        intent.putExtra("playOrPause", R.drawable.footpause);
                        sendBroadcast(intent);
                    }
                }
                break;
            case R.id.music_play:
                musicService.playOrPause();
                if (MusicService.mediaPlayer.isPlaying()) {
                    if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                        play_pause.setImageResource(R.drawable.footpausewhite);
                    else
                        play_pause.setImageResource(R.drawable.footpause);
                } else {
                    if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                        play_pause.setImageResource(R.drawable.footplaywhite);
                    else
                        play_pause.setImageResource(R.drawable.footplay);
                }
                break;
            case R.id.music_next:
                if (!MusicService.mediaPlayer.isPlaying()) {
                    if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                        play_pause.setImageResource(R.drawable.footpausewhite);
                    else
                        play_pause.setImageResource(R.drawable.footpause);
                }
                musicService.nextMusic();
                break;
            case R.id.music_last:
                if (!MusicService.mediaPlayer.isPlaying()) {
                    if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                        play_pause.setImageResource(R.drawable.footpausewhite);
                    else
                        play_pause.setImageResource(R.drawable.footpause);
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
                popupMenu.inflate(R.menu.slide_menu);

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
            case R.id.lyric:
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
                builder.setPositiveButton(R.string.delete_confrim, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (file.exists()) {
                            embedLyricFormFile(file, mp3File, music);
                        } else {
                            if (MusicUtils.loadWebLyric) {
                                switch (MusicUtils.loadlyric) {
                                    case 0:
                                        musicUtils.getWebLyric(MusicService.music.getTitle(),
                                                MusicService.music.getArtist(),
                                                false, true);
                                        break;
                                    case 1:
                                        musicUtils.getWebLyricFromNetease(MusicService.music.getTitle(),
                                                MusicService.music.getArtist(),
                                                false, true, false);
                                        break;
                                    case 2:
                                        musicUtils.getWebLyricFromKugou(MusicService.music.getTitle(),
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
                });
                builder.show();
            } else {
                if (file.exists()) {
                    embedLyricFormFile(file, mp3File, music);
                } else {
                    if (MusicUtils.loadWebLyric) {
                        switch (MusicUtils.loadlyric) {
                            case 0:
                                musicUtils.getWebLyric(MusicService.music.getTitle(),
                                        MusicService.music.getArtist(),
                                        false, true);
                                break;
                            case 1:
                                musicUtils.getWebLyricFromNetease(MusicService.music.getTitle(),
                                        MusicService.music.getArtist(),
                                        false, true, false);
                                break;
                            case 2:
                                musicUtils.getWebLyricFromKugou(MusicService.music.getTitle(),
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

    public void setLyric(Context context, MusicUtils musicUtils) {
        boolean canScroll = false;

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

        if (MusicUtils.downloadAlbum == 2 && MusicUtils.haveWIFI(context)
                || MusicUtils.downloadAlbum == 1) {

            Bitmap bitmap = GetAlbumArt.getAlbumArtBitmap(context, MusicService.music.getAlbumArtUri(), 1);
            if (bitmap != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                Glide.with(context)
                        .load(stream.toByteArray())
                        .placeholder(R.drawable.default_album_art)
                        .error(R.drawable.default_album_art)
                        .crossFade(1000)
                        .bitmapTransform(new BlurTransformation(context, 23, 4)) // “23”：设置模糊度(在0.0到25.0之间)，默认”25";"4":图片缩放比例,默认“1”。
                        .into(blurBG);
            } else {
                File blurFile = musicUtils.getAlbumCoverFile(MusicService.music.getArtist(), MusicService.music.getAlbum());
                if (blurFile.exists()) {
                    Glide.with(context)
                            .load(blurFile)
                            .placeholder(R.drawable.default_album_art)
                            .error(R.drawable.default_album_art)
                            .crossFade(1000)
                            .bitmapTransform(new BlurTransformation(context, 23, 4)) // “23”：设置模糊度(在0.0到25.0之间)，默认”25";"4":图片缩放比例,默认“1”。
                            .into(blurBG);

                } else {
                    musicUtils.getAlbumCover(MusicService.music.getArtist(),
                            MusicService.music.getAlbum(), MusicUtils.FROM_MAINIAMGE);
                }
            }
        } else {
            File blurFile = musicUtils.getAlbumCoverFile(MusicService.music.getArtist(), MusicService.music.getAlbum());
            if (blurFile.exists()) {
                Glide.with(context)
                        .load(blurFile)
                        .placeholder(R.drawable.default_album_art)
                        .error(R.drawable.default_album_art)
                        .crossFade(1000)
                        .bitmapTransform(new BlurTransformation(context, 23, 4)) // “23”：设置模糊度(在0.0到25.0之间)，默认”25";"4":图片缩放比例,默认“1”。
                        .into(blurBG);

            } else {
                Glide.with(context)
                        .load(MusicService.music.getAlbumArtUri())
                        .placeholder(R.drawable.default_album_art)
                        .error(R.drawable.default_album_art)
                        .crossFade(1000)
                        .bitmapTransform(new BlurTransformation(context, 23, 4)) // “23”：设置模糊度(在0.0到25.0之间)，默认”25";"4":图片缩放比例,默认“1”。
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
                        } else if (MusicUtils.enableTranslateLyric) {
                            setTranslateLyricFromFile(context, newSongTitle, newSinger);
                        }
                    } else if (MusicUtils.enableTranslateLyric) {
                        setTranslateLyricFromFile(context, newSongTitle, newSinger);
                    } else if (file.exists()) {
                        lyricView.loadLrc(file);
                        handler1.post(runnable1);
                    } else {
                        searchLyric(context);
                    }
                } else {
                    if (MusicUtils.enableTranslateLyric) {
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
        } else if (MusicUtils.enableTranslateLyric) {
            setTranslateLyricFromFile(context, newSongTitle, newSinger);
        } else if (file.exists()) {
            lyricView.loadLrc(file);
            handler1.post(runnable1);
        } else {
            searchLyric(context);
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

    private void embedLyricFormFile(File file, Mp3File mp3File, Music music) {
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

        MusicUtils musicUtils = new MusicUtils(getApplicationContext());

        if (MusicUtils.loadWebLyric) {
            switch (MusicUtils.loadlyric) {
                case 0:
                    musicUtils.getWebLyric(MusicService.music.getTitle(),
                            MusicService.music.getArtist(),
                            true, false);
                    break;
                case 1:
                    musicUtils.getWebLyricFromNetease(MusicService.music.getTitle(),
                            MusicService.music.getArtist(),
                            true, false, false);
                    break;
                case 2:
                    musicUtils.getWebLyricFromKugou(MusicService.music.getTitle(),
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

            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    "MusicPlayer/album");

            if (!dir.exists()) {
                dir.mkdirs();
            }

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

                if (albumFile.exists()) {
                    albumFile.delete();
                }

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

                            Intent intent4 = new Intent("notifyDataSetChanged");
                            sendBroadcast(intent4);

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            if (MusicService.mediaPlayer.isPlaying()) {
                lyricView.updateTime(MusicService.mediaPlayer.getCurrentPosition());
            }
            handler1.postDelayed(this, 100);
        }
    };

    private void playOrPauseMusic() {

        if (Mod != 1) {
            MusicListFragment.readMusic(this);
        }

        if (MusicListFragment.count == 0
                && ArtistDetailFragment.count == 0
                && AlbumDetailFragment.count == 0
                && MusicRecentAddedFragment.count == 0
                && PlaylistDetailFragment.count == 0
                && FolderDetailFragment.count == 0
                && count == 0
                && !MusicService.mediaPlayer.isPlaying()
                && MusicListFragment.musicList.size() >= 1
                && flag == 0) {

            startMusic(MusicUtils.pos);

            PlayOrPause.setImageResource(R.drawable.footpausewhite);

            if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                play_pause.setImageResource(R.drawable.footpausewhite);
            else
                play_pause.setImageResource(R.drawable.footpause);

            if (MusicService.isRandom) {
                if (shufflelist.size() >= 1) {
                    if (MusicUtils.enableDefaultCover) {
                        Glide.with(MusicList.this)
                                .load(R.drawable.default_album_art)
                                .into(mainImageView);
                    } else {
                        setAlbumCoverToMainImageView(MusicList.this,
                                musicUtils, shufflelist.get(MusicUtils.pos),
                                MusicUtils.FROM_MAINIAMGE);
                    }
                }
            } else {
                if (list.size() >= 1) {
                    if (MusicUtils.enableDefaultCover) {
                        Glide.with(MusicList.this)
                                .load(R.drawable.default_album_art)
                                .into(mainImageView);
                    } else {
                        setAlbumCoverToMainImageView(MusicList.this,
                                musicUtils, list.get(MusicUtils.pos),
                                MusicUtils.FROM_MAINIAMGE);
                    }
                }
            }
        } else if (MusicListFragment.count == 1
                || MusicRecentAddedFragment.count == 1
                || ArtistDetailFragment.count == 1
                || AlbumDetailFragment.count == 1
                || PlaylistDetailFragment.count == 1
                || FolderDetailFragment.count == 1
                || count == 1
                || flag == 1) {

            MusicService.mediaPlayer.start();

            if (flag == 1) {
                try {
                    Mp3File f = new Mp3File(MusicService.music.getUri());
                    if (f.getId3v2Tag().getLyrics() != null) {
                        lyricButton.setVisibility(View.VISIBLE);
                    } else {
                        lyricButton.setVisibility(View.GONE);
                    }
                } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                    e.printStackTrace();
                }
            }

            initMusicPlayer();

            PlayOrPause.setImageResource(R.drawable.footpausewhite);
            if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                play_pause.setImageResource(R.drawable.footpausewhite);
            else
                play_pause.setImageResource(R.drawable.footpause);

            if (MusicUtils.enableDefaultCover) {
                Glide.with(MusicList.this)
                        .load(R.drawable.default_album_art)
                        .into(mainImageView);
            } else {
                setAlbumCoverToMainImageView(MusicList.this,
                        musicUtils, MusicService.music, MusicUtils.FROM_MAINIAMGE);
            }
        }

        sendBroadcast(new Intent("SetClickable_False"));

        sendBroadcast(new Intent("update"));

        Intent intent = new Intent("setPlayOrPause");
        intent.putExtra("playOrPause", R.drawable.footpause);
        sendBroadcast(intent);

        initMusicPlayer();

        slideUp.show();
        PlayOrPause.hide();
        footBar.setVisibility(View.GONE);
    }

    //ListView和service绑定
    private void startMusic(int position) {

        Intent serviceIntent = new Intent(MusicList.this, MusicService.class);
        serviceIntent.setAction("musiclist");

        if (MusicListFragment.count == 1) {
            serviceIntent.putExtra("from", 1);

        } else if (ArtistDetailFragment.count == 1) {
            serviceIntent.putExtra("from", 2);

        } else if (AlbumDetailFragment.count == 1) {
            serviceIntent.putExtra("from", 3);

        } else if (PlaylistDetailFragment.count == 1) {
            serviceIntent.putExtra("from", 4);

        } else if (FolderDetailFragment.count == 1) {
            serviceIntent.putExtra("from", 5);

        } else if (MusicRecentAddedFragment.count == 1) {
            serviceIntent.putExtra("from", 6);

        } else {
            count = 1;
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
            if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                cyclePlay.setImageResource(R.drawable.cycle_true);
            else
                cyclePlay.setImageResource(R.drawable.cycle_white);
        } else if (MusicService.isSingleOrCycle == 2) {
            if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                cyclePlay.setImageResource(R.drawable.single_ture);
            else
                cyclePlay.setImageResource(R.drawable.single_white);
        } else {
            if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                cyclePlay.setImageResource(R.drawable.cycle_false);
            else
                cyclePlay.setImageResource(R.drawable.cycle_true);
        }


        if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
            play_pause.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.material_gray_hard)));
        else
            play_pause.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

        if (MusicService.mediaPlayer.isPlaying()) {
            if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                play_pause.setImageResource(R.drawable.footpausewhite);
            else
                play_pause.setImageResource(R.drawable.footpause);

        } else {
            if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                play_pause.setImageResource(R.drawable.footplaywhite);
            else
                play_pause.setImageResource(R.drawable.footplay);
        }

        if (!MusicService.isRandom) {
            if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                randomPlay.setImageResource(R.drawable.shuffle_false);
            else
                randomPlay.setImageResource(R.drawable.shuffle_true);
        } else {
            if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                randomPlay.setImageResource(R.drawable.shuffle_true);
            else
                randomPlay.setImageResource(R.drawable.shuffle_white);
        }
        if (MusicUtils.loadWebLyric) {
            lyricButton.setVisibility(View.VISIBLE);
            if (BaseActivity.isNight || !BaseActivity.changeMainWindow)
                lyricButton.setImageResource(R.drawable.lyric);
        }
        if (BaseActivity.isNight || !BaseActivity.changeMainWindow) {
            next.setImageResource(R.drawable.next);
            last.setImageResource(R.drawable.previous);
            slideMenu.setImageResource(R.drawable.list_menu);
        }
    }

    public final Handler handler = new Handler();
    public final Handler handler1 = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if (slideUp.isVisible()) {
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

            handler.postDelayed(runnable, 1000);

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meun, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void shufflePlay() {

        MusicUtils musicUtils = new MusicUtils(this);
        switch (Mod) {
            case 1:
                if (MusicListFragment.musicList.size() >= 1) {
                    MusicListFragment.pos = musicUtils.createRandom(MusicListFragment.musicList);
                    progress = 0;

                    ArtistDetailFragment.count = 0;
                    AlbumDetailFragment.count = 0;
                    PlaylistDetailFragment.count = 0;
                    MusicRecentAddedFragment.count = 0;
                    MusicListFragment.count = 1;
                    FolderDetailFragment.count = 0;
                    count = 0;

                    musicUtils.shufflePlay(MusicListFragment.musicList, 1);
                }
                break;
            case 2:
                if (ArtistListFragment.artistDetailFragment != null && ArtistDetailFragment.musicList.size() >= 1) {
                    ArtistDetailFragment.pos = musicUtils.createRandom(ArtistDetailFragment.musicList);
                    progress = 0;

                    ArtistDetailFragment.count = 1;
                    AlbumDetailFragment.count = 0;
                    PlaylistDetailFragment.count = 0;
                    MusicRecentAddedFragment.count = 0;
                    MusicListFragment.count = 0;
                    FolderDetailFragment.count = 0;
                    count = 0;

                    musicUtils.shufflePlay(ArtistDetailFragment.musicList, 2);
                }
                break;
            case 3:
                if (AlbumListFragment.detailFragment != null && AlbumDetailFragment.musicList.size() >= 1) {
                    AlbumDetailFragment.pos = musicUtils.createRandom(AlbumDetailFragment.musicList);
                    progress = 0;

                    ArtistDetailFragment.count = 0;
                    AlbumDetailFragment.count = 1;
                    PlaylistDetailFragment.count = 0;
                    MusicRecentAddedFragment.count = 0;
                    MusicListFragment.count = 0;
                    FolderDetailFragment.count = 0;
                    count = 0;

                    musicUtils.shufflePlay(AlbumDetailFragment.musicList, 3);

                }
                break;
            case 4:
                if (PlaylistFragment.playlistDetailFragment != null && PlaylistDetailFragment.musicList.size() >= 1) {
                    PlaylistDetailFragment.pos = musicUtils.createRandom(PlaylistDetailFragment.musicList);
                    progress = 0;

                    ArtistDetailFragment.count = 0;
                    AlbumDetailFragment.count = 0;
                    PlaylistDetailFragment.count = 1;
                    MusicRecentAddedFragment.count = 0;
                    MusicListFragment.count = 0;
                    FolderDetailFragment.count = 0;
                    count = 0;

                    musicUtils.shufflePlay(PlaylistDetailFragment.musicList, 4);
                }
                break;
            case 5:
                if (MusicRecentAddedFragment.musicList.size() >= 1) {
                    MusicRecentAddedFragment.pos = musicUtils.createRandom(MusicRecentAddedFragment.musicList);
                    progress = 0;

                    ArtistDetailFragment.count = 0;
                    AlbumDetailFragment.count = 0;
                    PlaylistDetailFragment.count = 0;
                    MusicRecentAddedFragment.count = 1;
                    MusicListFragment.count = 0;
                    FolderDetailFragment.count = 0;
                    count = 0;

                    musicUtils.shufflePlay(MusicRecentAddedFragment.musicList, 6);

                }
                break;
            case 6:
                if (FolderFragment.folderDetailFragment != null && FolderDetailFragment.musicList.size() >= 1) {
                    FolderDetailFragment.pos = musicUtils.createRandom(FolderDetailFragment.musicList);
                    progress = 0;

                    ArtistDetailFragment.count = 0;
                    AlbumDetailFragment.count = 0;
                    PlaylistDetailFragment.count = 0;
                    MusicRecentAddedFragment.count = 0;
                    MusicListFragment.count = 0;
                    FolderDetailFragment.count = 1;
                    count = 0;

                    musicUtils.shufflePlay(FolderDetailFragment.musicList, 5);
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
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        quit();
        unbindService(conn);
        super.onDestroy();
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

                musicUtils.cancelNetCall();
                lyricView.setVisibility(View.GONE);
                blurBG.setVisibility(View.GONE);
                footBar.setVisibility(View.VISIBLE);
                footBar.setVisibility(View.VISIBLE);

            } else {
                if (actionMode != null) {

                    Intent in = new Intent("notifyDataSetChanged");
                    MusicUtils.isSelectAll = false;

                    switch (Mod) {
                        case 1:
                            MusicListFragment.positionSet.clear();
                            sendBroadcast(in);
                            getWindow().setStatusBarColor(0);
                            break;
                        case 2:
                            if (MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_DETAIl_PAGE) {
                                AlbumDetailFragment.positionSet.clear();
                                sendBroadcast(in);
                                getWindow().setStatusBarColor(AlbumDetailFragment.playColor);
                            } else if ((MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_PAGE
                                    && ArtistDetailFragment.CLICK_SONGLIST)) {
                                ArtistDetailFragment.positionSet.clear();
                                sendBroadcast(in);
                                getWindow().setStatusBarColor(ArtistDetailFragment.playColor);
                            } else if ((MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_PAGE
                                    && ArtistDetailFragment.CLICK_ALBUMLIST)) {
                                ArtistDetailFragment.albumPositionSet.clear();
                                sendBroadcast(in);
                                getWindow().setStatusBarColor(ArtistDetailFragment.playColor);
                            } else if (artistFragment != null) {
                                ArtistListFragment.positionSet.clear();
                                toolbar.setBackgroundColor(0);
                                getWindow().setStatusBarColor(0);
                                sendBroadcast(in);
                            }
                            break;
                        case 3:
                            if (MusicUtils.fromWhere == MusicUtils.FROM_ALBUM_PAGE) {
                                AlbumDetailFragment.positionSet.clear();
                                sendBroadcast(in);
                                getWindow().setStatusBarColor(AlbumDetailFragment.playColor);
                            } else if (albumFragment != null) {
                                AlbumListFragment.positionSet.clear();
                                toolbar.setBackgroundColor(0);
                                getWindow().setStatusBarColor(0);
                                sendBroadcast(in);
                            }
                            break;
                        case 5:
                            MusicRecentAddedFragment.positionSet.clear();
                            sendBroadcast(in);
                            getWindow().setStatusBarColor(0);
                            break;
                        case 6:
                            if (FolderFragment.folderDetailFragment != null) {

                                FolderDetailFragment.positionSet.clear();
                                getWindow().setStatusBarColor(0);
                                toolbar.setBackgroundColor(0);
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
                                    && MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_DETAIl_PAGE) {

                                Intent intent = new Intent("hide albumDetailFragment");
                                sendBroadcast(intent);

                                getWindow().setStatusBarColor(ArtistDetailFragment.playColor);
                                MusicUtils.fromWhere = MusicUtils.FROM_ARTIST_PAGE;

                            } else if (ArtistListFragment.artistDetailFragment != null
                                    && MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_PAGE) {

                                getWindow().setStatusBarColor(0);
                                toolbar.setBackgroundColor(0);
                                transaction = fragmentManager.beginTransaction();
                                if (ArtistListFragment.artistDetailFragment != null)
                                    transaction.hide(ArtistListFragment.artistDetailFragment);
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
                                MusicUtils.fromWhere = MusicUtils.CLEAR;

                            } else if (artistFragment != null) {
                                moveTaskToBack(true);
                            }
                            break;
                        case 3:
                            if (AlbumListFragment.detailFragment != null) {
                                getWindow().setStatusBarColor(0);
                                toolbar.setBackgroundColor(0);
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
                            MusicUtils.fromWhere = MusicUtils.CLEAR;
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
                                    transaction.hide(PlaylistFragment.playlistDetailFragment);
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
                                getWindow().setStatusBarColor(0);
                                toolbar.setBackgroundColor(0);
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
            inflater.inflate(R.menu.menu_sub, menu);
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
        getWindow().setStatusBarColor(statusBarColor);
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

        if (MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_DETAIl_PAGE) {

            AlbumDetailFragment.positionSet.clear();
            sendBroadcast(in);
            getWindow().setStatusBarColor(AlbumDetailFragment.playColor);

        } else {

            switch (Mod) {
                case 1:
                    MusicListFragment.positionSet.clear();
                    sendBroadcast(in);
                    getWindow().setStatusBarColor(0);
                    break;
                case 2:
                    if (MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_DETAIl_PAGE) {
                        AlbumDetailFragment.positionSet.clear();
                        sendBroadcast(in);
                        getWindow().setStatusBarColor(AlbumDetailFragment.playColor);
                    } else if ((MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_PAGE
                            && ArtistDetailFragment.CLICK_SONGLIST)) {
                        ArtistDetailFragment.positionSet.clear();
                        sendBroadcast(in);
                        getWindow().setStatusBarColor(ArtistDetailFragment.playColor);
                    } else if ((MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_PAGE
                            && ArtistDetailFragment.CLICK_ALBUMLIST)) {
                        ArtistDetailFragment.albumPositionSet.clear();
                        sendBroadcast(in);
                        getWindow().setStatusBarColor(ArtistDetailFragment.playColor);
                    } else if (artistFragment != null) {
                        ArtistListFragment.positionSet.clear();
                        sendBroadcast(in);
                        getWindow().setStatusBarColor(0);
                    }
                    break;
                case 3:
                    if (AlbumListFragment.detailFragment == null && albumFragment != null) {
                        AlbumListFragment.positionSet.clear();
                        sendBroadcast(in);
                        getWindow().setStatusBarColor(0);
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
                    getWindow().setStatusBarColor(0);
                    break;
                case 6:
                    if (FolderFragment.folderDetailFragment != null) {
                        FolderDetailFragment.positionSet.clear();
                        getWindow().setStatusBarColor(0);
                        toolbar.setBackgroundColor(0);
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
                        musicUtils.selectAll(listPositionSet, MusicListFragment.musicList);
                        isArtist = false;
                        break;
                    case 2:
                        if (MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_DETAIl_PAGE) {

                            musicUtils.selectAll(listPositionSet, AlbumDetailFragment.musicList);
                            isArtist = false;

                        } else if ((MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_PAGE
                                && ArtistDetailFragment.CLICK_SONGLIST)) {

                            musicUtils.selectAll(listPositionSet, ArtistDetailFragment.musicList);
                            isArtist = false;

                        } else if ((MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_PAGE
                                && ArtistDetailFragment.CLICK_ALBUMLIST)) {
                            musicUtils.selectAll(listPositionSet, ArtistDetailFragment.albumList);
                            isAlbum = true;

                        } else {

                            musicUtils.selectAll(listPositionSet, ArtistListFragment.artistlist);
                            isArtist = true;
                        }

                        break;
                    case 3:
                        if (AlbumListFragment.detailFragment != null) {
                            musicUtils.selectAll(listPositionSet, AlbumDetailFragment.musicList);
                            isArtist = false;
                        } else {
                            musicUtils.selectAll(listPositionSet, AlbumListFragment.albumlist);
                            isAlbum = true;
                        }
                        break;
                    case 5:
                        musicUtils.selectAll(listPositionSet, MusicRecentAddedFragment.musicList);
                        isArtist = false;
                        break;
                    case 6:
                        if (FolderFragment.folderDetailFragment != null) {
                            musicUtils.selectAll(listPositionSet, FolderDetailFragment.musicList);
                            isArtist = false;
                        }
                        break;
                }
                sendBroadcast(intent);
                break;
            case R.id.add_to_list:
                switch (Mod) {
                    case 1:
                        if (!MusicUtils.isSelectAll) {
                            listPositionSet = MusicListFragment.positionSet;
                        }
                        musicList = MusicListFragment.musicList;
                        isArtist = false;
                        break;
                    case 2:
                        if (MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_DETAIl_PAGE) {

                            if (!MusicUtils.isSelectAll) {
                                listPositionSet = AlbumDetailFragment.positionSet;
                            }
                            musicList = AlbumDetailFragment.musicList;
                            isArtist = false;

                        } else if ((MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_PAGE
                                && ArtistDetailFragment.CLICK_SONGLIST)) {

                            if (!MusicUtils.isSelectAll) {
                                listPositionSet = ArtistDetailFragment.positionSet;
                            }
                            musicList = ArtistDetailFragment.musicList;
                            isArtist = false;

                        } else if ((MusicUtils.fromWhere == MusicUtils.FROM_ARTIST_PAGE
                                && ArtistDetailFragment.CLICK_ALBUMLIST)) {

                            if (!MusicUtils.isSelectAll) {
                                listPositionSet = ArtistDetailFragment.albumPositionSet;
                            }
                            musicList = ArtistDetailFragment.albumList;
                            isAlbum = true;
                            artistInALbum = ArtistDetailFragment.artist;

                        } else {

                            if (!MusicUtils.isSelectAll) {
                                listPositionSet = ArtistListFragment.positionSet;
                            }
                            musicList = ArtistListFragment.artistlist;
                            isArtist = true;

                        }

                        break;
                    case 3:
                        if (AlbumListFragment.detailFragment != null
                                && MusicUtils.fromWhere == MusicUtils.FROM_ALBUM_PAGE) {

                            if (!MusicUtils.isSelectAll) {
                                listPositionSet = AlbumDetailFragment.positionSet;
                            }
                            musicList = AlbumDetailFragment.musicList;
                            isArtist = false;

                        } else {
                            if (!MusicUtils.isSelectAll) {
                                listPositionSet = AlbumListFragment.positionSet;
                            }
                            musicList = AlbumListFragment.albumlist;
                            isAlbum = true;
                        }
                        break;
                    case 5:
                        if (!MusicUtils.isSelectAll) {
                            listPositionSet = MusicRecentAddedFragment.positionSet;
                        }
                        musicList = MusicRecentAddedFragment.musicList;
                        isArtist = false;
                        break;
                    case 6:
                        if (FolderFragment.folderDetailFragment != null) {
                            if (!MusicUtils.isSelectAll) {
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
                    case "Music play to the end":
                        if (MusicUtils.enableDefaultCover) {
                            Glide.with(MusicList.this)
                                    .load(R.drawable.default_album_art)
                                    .into(mainImageView);
                        } else {
                            setAlbumCoverToMainImageView(MusicList.this,
                                    musicUtils, MusicService.music, MusicUtils.FROM_MAINIAMGE);
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
                        PlayOrPause.setImageResource(R.drawable.footplaywhite);
                        Log.d("FLAG RESET TO 0", "FLAG RESET TO 0");
                        break;
                    case "search_play":

                        footTitle.setText(MusicService.music.getTitle());
                        footArtist.setText(MusicService.music.getArtist());
                        PlayOrPause.setImageResource(R.drawable.footpausewhite);

                        musicUtils.setAlbumCoverToFootAndHeader(MusicService.music, MusicUtils.FROM_FOOTBAR);

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
                                musicUtils, MusicService.music, MusicUtils.FROM_MAINIAMGE);
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
                        MusicUtils.loadImageUseGlide(context, footAlbumArt, picUri, false);
                        MusicUtils.loadImageUseGlide(context, accountHeader.getHeaderBackgroundView(),
                                picUri, true);
                        Log.d("load footbar cover", "load footbar cover");
                        break;
                    case "load album cover":
                        String uri = intent.getStringExtra("picUrl");
                        MusicUtils.loadImageUseGlide(context, mainImageView, uri, false);
                        MusicUtils.loadImageUseGlide(context, footAlbumArt, uri, false);
                        MusicUtils.loadImageUseGlide(context, accountHeader.getHeaderBackgroundView(),
                                uri, true);
                        Log.d("load album cover", "load album cover");

                        break;
                    case "load album cover failed":
                        Glide.with(context)
                                .load(R.drawable.default_album_art)
                                .into(mainImageView);
                        Log.d("load album cover failed", "load album cover failed");

                        Glide.with(context)
                                .load(R.drawable.default_album_art)
                                .centerCrop()
                                .into(footAlbumArt);
                        Glide.with(context)
                                .load(R.drawable.default_album_art_land)
                                .centerCrop()
                                .into(accountHeader.getHeaderBackgroundView());
                        break;
                    case "load footbar cover failed":
                        Glide.with(context)
                                .load(R.drawable.default_album_art)
                                .centerCrop()
                                .into(footAlbumArt);
                        Glide.with(context)
                                .load(R.drawable.default_album_art_land)
                                .centerCrop()
                                .into(accountHeader.getHeaderBackgroundView());
                        break;
                    case "show blurBG":
                        if (blurBG.getVisibility() == View.VISIBLE) {
                            Glide.with(context)
                                    .load(intent.getStringExtra("picUrl"))
                                    .placeholder(R.drawable.default_album_art)
                                    .error(R.drawable.default_album_art)
                                    .crossFade(1000)
                                    .bitmapTransform(new BlurTransformation(context, 23, 4)) // “23”：设置模糊度(在0.0到25.0之间)，默认”25";"4":图片缩放比例,默认“1”。
                                    .into(blurBG);
                            Log.d("show blurBG", "show blurBG");
                        }
                        break;
                    case "restart yourself":
                        finish();
                        Intent themeIntent = getIntent();
                        themeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(themeIntent);
                        overridePendingTransition(0, 0);
                        break;
                    case "set toolbar gone":
                        toolbar.setVisibility(View.GONE);
                        break;
                    case "set toolbar text":
                        toolbar.setBackgroundColor(0);
                        toolbar.setVisibility(View.VISIBLE);
                        toolbar.setTitle(intent
                                .getIntExtra("title", R.string.toolbar_title_music_player));
                        break;
                    case "set toolbar clear":
                        toolbar.setBackgroundColor(0);
                        break;
                    case "set toolbar color":
                        toolbar.setBackgroundColor(intent.getIntExtra("color", 0));
                        break;
                    case "set PlayOrPause":
                        PlayOrPause.setImageResource(intent.getIntExtra("PlayOrPause", R.drawable.footpausewhite));
                        break;
                    case "set footBar":
                        footTitle.setText(intent.getStringExtra("footTitle"));
                        footArtist.setText(intent.getStringExtra("footArtist"));
                        break;
                    case "load image with uri":
                        MusicUtils.loadImageUseGlide(
                                getApplicationContext(), footAlbumArt,
                                intent.getStringExtra("uri"), false);

                        MusicUtils.loadImageUseGlide(
                                getApplicationContext(), accountHeader.getHeaderBackgroundView(),
                                intent.getStringExtra("uri"), true);
                        break;
                    case "show snackBar":
                        Snackbar.make(PlayOrPause, intent.getIntExtra("text id", R.string.unknown_error), Snackbar.LENGTH_SHORT)
                                .setDuration(1000)
                                .show();
                        break;
                    case "open drawer":
                        slideDrawer.openDrawer();
                        break;
                    case "set lyricButton visibility":
                        lyricButton.setVisibility(intent.getIntExtra("visibility", 0));
                        break;
                    case "set lyricView visibility":
                        lyricView.setVisibility(intent.getIntExtra("visibility", 0));
                        break;
                    case "set blurBG visibility":
                        blurBG.setVisibility(intent.getIntExtra("visibility", 0));
                        break;
                    case "set lyric from service":
                        if (lyricView.getVisibility() == View.VISIBLE)
                            setLyric(getApplicationContext(), musicUtils);
                        break;
                    case "get blurBG visibility":
                        if (blurBG.getVisibility() == View.VISIBLE) {
                            Intent intent2 = new Intent("show blurBG");
                            intent2.putExtra("file", intent.getStringExtra("file"));
                            sendBroadcast(intent2);
                        }
                        break;
                    case "keep screen on":
                        if (MusicUtils.keepScreenOn)
                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        else
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        break;
                }
            }
        }
    }

    private static IntentFilter musicFinishFilter() {
        IntentFilter intentfilter = new IntentFilter();
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
        intentfilter.addAction("set lyricButton visibility");
        intentfilter.addAction("set lyricView visibility");
        intentfilter.addAction("set blurBG visibility");
        intentfilter.addAction("set lyric from service");
        intentfilter.addAction("get blurBG visibility");
        intentfilter.addAction("show fragment");
        intentfilter.addAction("hide fragment");
        intentfilter.addAction("keep screen on");
        return intentfilter;
    }

    private void setAlbumCoverToMainImageView(Context context, MusicUtils musicUtils, Music music, int from) {
        if (MusicUtils.downloadAlbum == 2 && MusicUtils.haveWIFI(context)
                || MusicUtils.downloadAlbum == 1) {

            Bitmap bitmap = GetAlbumArt.getAlbumArtBitmap(context, music.getAlbumArtUri(), 1);
            if (bitmap != null) {
                MusicUtils.loadImageUseGlide(context, mainImageView, bitmap);
            } else {
                File file = musicUtils.getAlbumCoverFile(music.getArtist(), music.getAlbum());
                if (file.exists()) {
                    MusicUtils.loadImageUseGlide(context, mainImageView, file);
                } else {
                    musicUtils.getAlbumCover(music.getArtist(),
                            music.getAlbum(), from);
                }
            }
        } else {
            File file = musicUtils.getAlbumCoverFile(music.getArtist(), music.getAlbum());
            if (file.exists())
                MusicUtils.loadImageUseGlide(context, mainImageView, file);
            else
                MusicUtils.loadImageUseGlide(context, mainImageView,
                        music.getAlbumArtUri(), false);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        updateSizeInfo();
    }

    //动态调整播放页面布局大小
    private void updateSizeInfo() {
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            int actionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data, getResources().getDisplayMetrics());

            RelativeLayout top_blank = findViewById(R.id.top_blank);
            RelativeLayout middle = findViewById(R.id.middle);
            RelativeLayout music_control = findViewById(R.id.music_control);
            RelativeLayout music_track = findViewById(R.id.music_track);
            LinearLayout middle_layout = findViewById(R.id.middle_layout);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) top_blank.getLayoutParams();
            params.height = actionBarHeight - actionBarHeight / 5;
            top_blank.setLayoutParams(params);

            int size = middle_layout.getHeight();

            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) middle.getLayoutParams();
            params2.height = size * 3 / 15;
            middle.setLayoutParams(params2);

            LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams) music_control.getLayoutParams();
            params3.height = size * 10 / 15;
            music_control.setLayoutParams(params3);

            LinearLayout.LayoutParams params4 = (LinearLayout.LayoutParams) music_track.getLayoutParams();
            params4.height = size * 2 / 15;
            music_track.setLayoutParams(params4);

        }
    }

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
        lyricButton = findViewById(R.id.lyric);
        blurBG = findViewById(R.id.album_art_blur);
        toolbar = findViewById(R.id.toolbar);


        PlayOrPause.setOnClickListener(this);
        slideMenu.setOnClickListener(this);
        play_pause.setOnClickListener(this);
        next.setOnClickListener(this);
        last.setOnClickListener(this);
        randomPlay.setOnClickListener(this);
        cyclePlay.setOnClickListener(this);
        lyricButton.setOnClickListener(this);
        fragmentManager = getFragmentManager();

        mainImageView.setOnTouchListener((v, event) -> {

            slideUp.onTouch(slideView, event);

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                xDown = event.getX();
                yDown = event.getY();
                pressTime = System.currentTimeMillis();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {// 松开处理
                releaseTime = System.currentTimeMillis();

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

                if (releaseTime - pressTime <= 400) {

                    if (MusicUtils.enableSwipeGesture) {
                        if ((xUp - xDown) > swipe) {
                            if (MusicListFragment.count == 1
                                    || ArtistDetailFragment.count == 1
                                    || AlbumDetailFragment.count == 1
                                    || MusicRecentAddedFragment.count == 1
                                    || PlaylistDetailFragment.count == 1
                                    || FolderDetailFragment.count == 1
                                    || count == 1
                                    || MusicService.mediaPlayer.isPlaying()
                                    || flag == 1)
                                musicService.preMusic();
                        } else if ((xUp - xDown) < -swipe) {
                            if (MusicListFragment.count == 1
                                    || ArtistDetailFragment.count == 1
                                    || AlbumDetailFragment.count == 1
                                    || MusicRecentAddedFragment.count == 1
                                    || PlaylistDetailFragment.count == 1
                                    || FolderDetailFragment.count == 1
                                    || count == 1
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
                            setLyric(getApplicationContext(), musicUtils);
                    }
                } else {
                    if (5 >= Math.abs(xDown - xUp) && 5 >= Math.abs(yDown - yUp)) {
                        if (lyricView.getVisibility() == View.GONE) {
                            setMainPopupDialog(this);
                        } else if (lyricView.getVisibility() == View.VISIBLE) {
                            setLyricPopupDialog(this);
                        }
                    }
                }
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

                if ((xUp - xDown) > swipe && 20 >= Math.abs(yDown - yUp)) {
                    if (MusicListFragment.count == 1
                            || ArtistDetailFragment.count == 1
                            || AlbumDetailFragment.count == 1
                            || MusicRecentAddedFragment.count == 1
                            || PlaylistDetailFragment.count == 1
                            || FolderDetailFragment.count == 1
                            || count == 1
                            || MusicService.mediaPlayer.isPlaying()
                            || flag == 1)
                        musicService.preMusic();
                } else if ((xUp - xDown) < -swipe && 20 >= Math.abs(yDown - yUp)) {
                    if (MusicListFragment.count == 1
                            || ArtistDetailFragment.count == 1
                            || AlbumDetailFragment.count == 1
                            || MusicRecentAddedFragment.count == 1
                            || PlaylistDetailFragment.count == 1
                            || FolderDetailFragment.count == 1
                            || count == 1
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
                                TagEditActivty.class);
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
                        lyricBuilder = new AlertDialog.Builder(PlayOrPause.getContext(),R.style.MaterialThemeDialog);
                    else
                        lyricBuilder = new AlertDialog.Builder(PlayOrPause.getContext());

                    lyricBuilder.setTitle(R.string.update_lyric);
                    lyricBuilder.setSingleChoiceItems(update, MusicUtils.updateMusic,
                        (dialogInterface1, i1) -> {
                            MusicUtils.updateMusic = i1;
                            SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                            editor.putInt("updateMusic", MusicUtils.updateMusic);
                            editor.apply();
                            switch (MusicUtils.updateMusic) {
                                case 0:
                                    if (lyricView != null)
                                        musicUtils.updateLyricFromNetease(
                                                MusicService.music.getTitle(),
                                                MusicService.music.getArtist(),
                                                true);
                                    else
                                        musicUtils.updateLyricFromNetease(
                                                MusicService.music.getTitle(),
                                                MusicService.music.getArtist(),
                                                false);
                                    break;
                                case 1:
                                    if (lyricView != null)
                                        musicUtils.updateLyricFromKugou(
                                                MusicService.music.getTitle(),
                                                MusicService.music.getArtist(),
                                                true);
                                    else
                                        musicUtils.updateLyricFromKugou(
                                                MusicService.music.getTitle(),
                                                MusicService.music.getArtist(),
                                                false);
                                    break;
                            }
                            dialogInterface1.cancel();
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
                                        musicUtils.getLyricFromNeteaseById(
                                                et.getText().toString(),
                                                MusicService.music.getTitle(),
                                                MusicService.music.getArtist(),
                                                true, false, false);
                                    } else {
                                        musicUtils.getLyricFromNeteaseById(
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




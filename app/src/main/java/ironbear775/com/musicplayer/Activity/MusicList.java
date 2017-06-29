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
import android.database.sqlite.SQLiteDatabase;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mancj.slideup.SlideUp;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import ironbear775.com.musicplayer.Util.Lyric.LrcView;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.Notification;
import ironbear775.com.musicplayer.Util.PlaylistDbHelper;
import ironbear775.com.musicplayer.Util.PlaylistDialog;
import ironbear775.com.musicplayer.Util.SquareImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by ironbear on 2016/12/9.
 */

public class MusicList extends BaseActivity implements Serializable, View.OnClickListener
        , ActionMode.Callback {
    public static TextView footTitle;
    public static TextView footArtist;
    public static ImageView footAlbumArt;
    public static FloatingActionButton PlayOrPause;
    private FloatingActionButton play_pause;
    public static AccountHeader accountHeader;
    public static Fragment albumFragment;
    public static Fragment artistFragment;
    public static Fragment playlistFragment;
    public static Fragment folderFragment;
    public static int Mod = 1;
    public static ActionMode actionMode;
    public static boolean isAlbum = false;
    public static boolean isArtist = false;
    public static int flag = 0;

    private ImageView slideMenu;
    private SlideUp slideUp;
    private int progress = 0;
    private MusicService musicService;
    public static Toolbar toolbar;
    private SquareImageView mainImageView;
    private TextView title, artist, album, duration, current;
    private ImageView cyclePlay;
    private ImageView randomPlay;
    private SeekBar musicProgress;
    private final SimpleDateFormat time = new SimpleDateFormat("m:ss");
    private RelativeLayout footBar;
    public static Drawer slideDrawer;
    private ProgressBar musicProgressBar;
    private android.app.Fragment musicFragment, musicRecentFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private ArrayList<Music> musicList = new ArrayList<>();
    public static Set<Integer> listPositionSet = new HashSet<>();
    private MusicUtils musicUtils;
    private View slideView;
    private Notification noti;
    public static int statusBarColor;
    public static LrcView lyricView;
    public static ImageView lyricButton;
    public static SquareImageView blurBG;
    private float xDown, yDown, xUp;

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

        MusicUtils.isFlyme = MusicUtils.isFlyme(this, "meizu");

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        ComponentName audiobutton = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());

        audioManager.registerMediaButtonEventReceiver(audiobutton);

        findView();

        init();

        toolbar.inflateMenu(R.menu.menu_short);
    }

    private void init() {

        statusBarColor = getWindow().getStatusBarColor();

        musicUtils = new MusicUtils(this);

        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);

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
        MusicUtils.enableLockscreenNotification = sharedPreferences.getBoolean("enableLockscreenNotification", true);
        MusicUtils.keepScreenOn = sharedPreferences.getBoolean("keepScreenOn", false);
        MusicUtils.launchPage = sharedPreferences.getInt("launchPage", 1);
        MusicUtils.filterNum = sharedPreferences.getInt("filterNum", 2);
        MusicUtils.loadWebLyric = sharedPreferences.getBoolean("loadWebLyric", true);
        MusicUtils.enableShuffle = sharedPreferences.getBoolean("enableShuffle", true);
        MusicUtils.downloadArtist = sharedPreferences.getInt("downloadArtist", 2);
        if (MusicUtils.keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(100);

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
        if (flag == 0) {
            musicService = new MusicService();
            Intent intent1 = new Intent(MusicList.this, MusicService.class);
            bindService(intent1, conn, Service.BIND_AUTO_CREATE);
            PlayOrPause.setImageResource(R.drawable.footplaywhite);
        } else {
            musicService = MusicService.musicService;
        }

        //toolbar
        toolbar.setTitle(getResources().getString(R.string.toolbar_title_music_player));

        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.listView_bg_color));
        setSupportActionBar(toolbar);

        initSlideDrawer();

        registerReceiver(musicFinishReceiver, musicFinishFilter());

        slideUp = new SlideUp.Builder(slideView)
                .withStartGravity(Gravity.BOTTOM)
                .withGesturesEnabled(true)
                .withTouchableArea(1000)
                .withStartState(SlideUp.State.HIDDEN)
                .withListeners(new SlideUp.Listener() {
                    @Override
                    public void onSlide(float percent) {
                        slideView.setAlpha(1 - (percent / 100));
                    }

                    @Override
                    public void onVisibilityChanged(int visibility) {
                        if (visibility == View.GONE) {

                            Intent intent = new Intent("SetClickable_True");
                            sendBroadcast(intent);

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
                Mod = 2;
                if (artistFragment == null || flag == 1) {
                    artistFragment = new ArtistListFragment();
                    transaction.add(R.id.content, artistFragment);
                    transaction.addToBackStack(null);
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
                Mod = 3;
                if (albumFragment == null || flag == 1) {
                    albumFragment = new AlbumListFragment();
                    transaction.add(R.id.content, albumFragment);
                    transaction.addToBackStack(null);
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
                Mod = 4;
                if (playlistFragment == null || flag == 1) {
                    playlistFragment = new PlaylistFragment();
                    transaction.add(R.id.content, playlistFragment);
                    transaction.addToBackStack(null);
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
                Mod = 6;
                if (folderFragment == null || flag == 1) {
                    folderFragment = new FolderFragment();
                    transaction.add(R.id.content, folderFragment);
                    transaction.addToBackStack(null);
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

        if (!MusicService.mediaPlayer.isPlaying()) {
            MusicService.mediaPlayer.pause();
            progress = MusicService.mediaPlayer.getCurrentPosition();
            Intent intent = new Intent(MusicList.this, MusicService.class);
            stopService(intent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
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
                .withIcon(R.drawable.album)
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

        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withOnAccountHeaderSelectionViewClickListener(new AccountHeader.OnAccountHeaderSelectionViewClickListener() {
                    @Override
                    public boolean onClick(View view, IProfile profile) {
                        slideDrawer.closeDrawer();
                        playOrPauseMusic();
                        return false;
                    }
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
                        setting,
                        equalizer

                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

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
                                Intent timerIntent = new Intent(MusicList.this, SleepTimer.class);
                                startActivity(timerIntent);
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
                    }
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
                        play_pause.setImageResource(R.drawable.footplaywhite);

                        MusicUtils.saveInfoService(getApplicationContext());
                    } else if (MusicListFragment.count == 0
                            && AlbumDetailFragment.count == 0
                            && MusicRecentAddedFragment.count == 0
                            && ArtistDetailFragment.count == 0
                            && PlaylistDetailFragment.count == 0
                            && FolderDetailFragment.count == 0
                            && !MusicService.mediaPlayer.isPlaying()
                            && flag == 0) {
                        startMusic(MusicUtils.pos);
                        PlayOrPause.setImageResource(R.drawable.footpausewhite);
                        play_pause.setImageResource(R.drawable.footpausewhite);

                    } else {
                        MusicService.mediaPlayer.start();
                        PlayOrPause.setImageResource(R.drawable.footpausewhite);
                        play_pause.setImageResource(R.drawable.footpausewhite);
                    }
                }
                break;
            case R.id.music_play:
                musicService.playOrPause();
                if (MusicService.mediaPlayer.isPlaying()) {
                    play_pause.setImageResource(R.drawable.footpausewhite);
                } else {
                    play_pause.setImageResource(R.drawable.footplaywhite);
                }
                break;
            case R.id.music_next:
                if (!MusicService.mediaPlayer.isPlaying()) {
                    play_pause.setImageResource(R.drawable.footpausewhite);
                }
                musicService.nextMusic();
                break;
            case R.id.music_last:
                if (!MusicService.mediaPlayer.isPlaying()) {
                    play_pause.setImageResource(R.drawable.footpausewhite);
                }
                musicService.preMusic();
                break;
            case R.id.random_play:
                MusicService.isRandom = !MusicService.isRandom;
                break;
            case R.id.cycle_play:
                if (MusicService.isSingleOrCycle == 1) {
                    MusicService.isSingleOrCycle = 2;
                } else if (MusicService.isSingleOrCycle == 2) {
                    MusicService.isSingleOrCycle = 3;
                } else {
                    MusicService.isSingleOrCycle = 1;
                }
                break;
            case R.id.slide_menu:
                PopupMenu popupMenu = new PopupMenu(this, slideMenu);
                popupMenu.inflate(R.menu.slide_menu);

                ArrayList<Music> mList = new ArrayList<>();
                mList.clear();
                mList.add(MusicService.music);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_add:
                                Set<Integer> listPositionSet = new HashSet<>();
                                listPositionSet.add(0);
                                PlaylistDialog dialog = new PlaylistDialog(
                                        MusicList.this, listPositionSet, mList);
                                dialog.show();
                                break;
                            case R.id.menu_delete:
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MusicList.this);
                                alertDialog.setTitle(R.string.delete_alert_title);
                                alertDialog.setMessage(mList.get(0).getTitle());
                                alertDialog.setCancelable(true);
                                alertDialog.setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                                alertDialog.setPositiveButton(R.string.delete_confrim, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String uri = mList.get(0).getUri();
                                        getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                MediaStore.Audio.Media.DATA + "=?",
                                                new String[]{uri});
                                        File file = new File(uri);
                                        if (file.isFile()) {
                                            if (file.delete()) {
                                                Intent intent = new Intent("notifyDataSetChanged");

                                                sendBroadcast(intent);
                                                Toast.makeText(getApplicationContext(),
                                                        R.string.success, Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(),
                                                        R.string.failed, Toast.LENGTH_SHORT).show();
                                            }
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
                            case R.id.tag_edit:
                                if (mList.get(0).getUri().contains(".mp3")) {
                                    Intent intent = new Intent(getApplicationContext(), TagEditActivty.class);
                                    intent.putExtra("music", (Parcelable) mList.get(0));
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.open_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case R.id.select_local_lyric:
                                Intent i = new Intent(getApplicationContext(), FilePickerActivity.class);

                                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

                                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                                startActivityForResult(i, 1313);
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
                break;
            case R.id.lyric:
                if (lyricView.getVisibility() == View.VISIBLE) {
                    lyricView.setVisibility(View.GONE);
                    blurBG.setVisibility(View.GONE);
                } else
                    setLyric(this);
                break;
        }

    }

    public static void setLyric(Context context) {
        boolean canScroll = false;

        String newSongTitle, newSinger;
        if (MusicService.music.getTitle().contains("/")) {
            newSongTitle = MusicService.music.getTitle().replace("/", "_");
        } else {
            newSongTitle = MusicService.music.getTitle();
        }
        if (MusicService.music.getArtist().contains("/")) {
            newSinger = MusicService.music.getArtist().replace("/", "_");
        } else {
            newSinger = MusicService.music.getArtist();
        }

        File file = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/MusicPlayer/lyric",
                newSongTitle + "_" + newSinger + ".lrc");

        Glide.with(context)
                .load(MusicService.music.getAlbumArtUri())
                .placeholder(R.drawable.default_album_art)
                .error(R.drawable.default_album_art)
                .crossFade(1000)
                .bitmapTransform(new BlurTransformation(context, 23, 4)) // “23”：设置模糊度(在0.0到25.0之间)，默认”25";"4":图片缩放比例,默认“1”。
                .into(blurBG);
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
                            handler.post(runnable1);
                        } else if (file.exists()) {
                            lyricView.loadLrc(file);
                            handler.post(runnable1);
                        } else {
                            lyricView.setLabel(context.getResources().getString(R.string.searching_lyric));
                            if (MusicUtils.loadWebLyric)
                                MusicUtils.getWebLyric(MusicService.music.getTitle(),
                                        MusicService.music.getArtist(),
                                        MusicService.music.getDuration());
                            else
                                lyricView.setLabel(context.getResources().getString(R.string.no_lyric));
                        }
                    } else if (file.exists()) {
                        lyricView.loadLrc(file);
                        handler.post(runnable1);
                    } else {
                        lyricView.setLabel(context.getResources().getString(R.string.searching_lyric));
                        if (MusicUtils.loadWebLyric)
                            MusicUtils.getWebLyric(MusicService.music.getTitle(),
                                    MusicService.music.getArtist(),
                                    MusicService.music.getDuration());
                        else
                            lyricView.setLabel(context.getResources().getString(R.string.no_lyric));

                    }
                }

            } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                e.printStackTrace();
            }
        } else if (file.exists()) {
            lyricView.loadLrc(file);
            handler.post(runnable1);
        } else {
            lyricView.setLabel(context.getResources().getString(R.string.searching_lyric));
            if (MusicUtils.loadWebLyric)
                MusicUtils.getWebLyric(MusicService.music.getTitle(),
                        MusicService.music.getArtist(),
                        MusicService.music.getDuration());
            else
                lyricView.setLabel(context.getResources().getString(R.string.no_lyric));

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
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
    }

    public static Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            if (MusicService.mediaPlayer.isPlaying()) {
                long time = MusicService.mediaPlayer.getCurrentPosition();
                lyricView.updateTime(time);
            }

            handler.postDelayed(this, 100);
        }
    };

    private void playOrPauseMusic() {

        if (MusicListFragment.count == 0
                && ArtistDetailFragment.count == 0
                && AlbumDetailFragment.count == 0
                && MusicRecentAddedFragment.count == 0
                && PlaylistDetailFragment.count == 0
                && FolderDetailFragment.count == 0
                && !MusicService.mediaPlayer.isPlaying()
                && MusicListFragment.musicList.size() >= 1
                && flag == 0) {
            startMusic(MusicUtils.pos);
            if (Mod == 1)
                MusicListFragment.count = 1;
            if (Mod == 2)
                ArtistDetailFragment.count = 1;
            if (Mod == 3)
                AlbumDetailFragment.count = 1;
            if (Mod == 4)
                PlaylistDetailFragment.count = 1;
            if (Mod == 5)
                MusicRecentAddedFragment.count = 1;
            if (Mod == 6)
                FolderDetailFragment.count = 1;

            PlayOrPause.setImageResource(R.drawable.footpausewhite);
            play_pause.setImageResource(R.drawable.footpausewhite);

            if (MusicListFragment.musicList.size() >= 1) {

                if (MusicUtils.enableDefaultCover) {
                    Glide.with(MusicList.this)
                            .load(R.drawable.default_album_art)
                            .error(R.drawable.default_album_art)
                            .into(mainImageView);
                } else {

                    Glide.with(MusicList.this)
                            .load(MusicListFragment.musicList.get(MusicUtils.pos).getAlbumArtUri())
                            .crossFade(500)
                            .error(R.drawable.default_album_art)
                            .into(mainImageView);
                }
            }
        } else if (MusicListFragment.count == 1
                || MusicRecentAddedFragment.count == 1
                || ArtistDetailFragment.count == 1
                || AlbumDetailFragment.count == 1
                || PlaylistDetailFragment.count == 1
                || FolderDetailFragment.count == 1
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
            play_pause.setImageResource(R.drawable.footpausewhite);

            if (MusicUtils.enableDefaultCover) {
                Glide.with(MusicList.this)
                        .load(R.drawable.default_album_art)
                        .error(R.drawable.default_album_art)
                        .into(mainImageView);
            } else {

                Glide.with(MusicList.this)
                        .load(MusicService.music.getAlbumArtUri())
                        .crossFade(500)
                        .error(R.drawable.default_album_art)
                        .into(mainImageView);
            }
        }

        Intent intent = new Intent("SetClickable_False");
        sendBroadcast(intent);

        Intent intent1 = new Intent("update");
        sendBroadcast(intent1);

        slideUp.show();

        PlayOrPause.hide();

        footBar.setVisibility(View.GONE);
    }

    //ListView和service绑定
    private void startMusic(int position) {

        Intent serviceIntent = new Intent(MusicList.this, MusicService.class);
        serviceIntent.setAction("musiclist");

        if (MusicListFragment.count == 1) {
            serviceIntent.putParcelableArrayListExtra("musicList", MusicListFragment.musicList);

        } else if (AlbumDetailFragment.count == 1) {
            serviceIntent.putParcelableArrayListExtra("musicList", AlbumDetailFragment.musicList);

        } else if (MusicRecentAddedFragment.count == 1) {
            serviceIntent.putParcelableArrayListExtra("musicList", MusicRecentAddedFragment.musicList);

        } else if (ArtistDetailFragment.count == 1) {
            serviceIntent.putParcelableArrayListExtra("musicList", ArtistDetailFragment.musicList);

        } else if (PlaylistDetailFragment.count == 1) {
            serviceIntent.putParcelableArrayListExtra("musicList", PlaylistDetailFragment.musicList);

        } else if (FolderDetailFragment.count == 1) {
            serviceIntent.putParcelableArrayListExtra("musicList", FolderDetailFragment.musicList);

        } else {
            serviceIntent.putParcelableArrayListExtra("musicList", MusicListFragment.musicList);
            MusicListFragment.count = 1;
        }

        serviceIntent.putExtra("musicPosition", position);
        serviceIntent.putExtra("musicProgress", progress);
        startService(serviceIntent);

        initMusicPlayer();
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
            cyclePlay.setImageResource(R.drawable.cycle_true);
        } else if (MusicService.isSingleOrCycle == 2) {
            cyclePlay.setImageResource(R.drawable.single_ture);
        } else {
            cyclePlay.setImageResource(R.drawable.cycle_false);
        }

        if (MusicService.mediaPlayer.isPlaying()) {
            play_pause.setImageResource(R.drawable.footpausewhite);
        } else {
            play_pause.setImageResource(R.drawable.footplaywhite);
        }

        if (!MusicService.isRandom) {
            randomPlay.setImageResource(R.drawable.shuffle_false);
        } else {
            randomPlay.setImageResource(R.drawable.shuffle_true);
        }
        if (MusicUtils.loadWebLyric)
            lyricButton.setVisibility(View.VISIBLE);

    }

    public static final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            musicProgressBar.setMax(MusicService.mediaPlayer.getDuration());
            musicProgressBar.setProgress(MusicService.mediaPlayer.getCurrentPosition());

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

            handler.postDelayed(runnable, 500);

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
                Intent timerIntent = new Intent(MusicList.this, SleepTimer.class);
                startActivity(timerIntent);
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

                lyricView.setVisibility(View.GONE);
                blurBG.setVisibility(View.GONE);
                footBar.setVisibility(View.VISIBLE);
                footBar.setVisibility(View.VISIBLE);
            } else {
                Intent in = new Intent("notifyDataSetChanged");
                MusicUtils.isSelectAll = false;
                switch (Mod) {
                    case 1:
                        if (actionMode != null) {
                            actionMode = null;
                            MusicListFragment.positionSet.clear();
                            sendBroadcast(in);
                        } else {
                            moveTaskToBack(true);
                        }
                        break;
                    case 2:
                        if (actionMode != null && ArtistListFragment.artistDetailFragment == null) {
                            actionMode = null;
                            ArtistListFragment.positionSet.clear();
                            sendBroadcast(in);
                        } else if (actionMode != null && ArtistListFragment.artistDetailFragment != null) {
                            actionMode = null;
                            ArtistDetailFragment.positionSet.clear();
                            getWindow().setStatusBarColor(0);
                            toolbar.setBackgroundColor(0);
                            sendBroadcast(in);
                        } else if (ArtistListFragment.artistDetailFragment != null) {
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
                            MusicList.toolbar.setVisibility(View.VISIBLE);
                            MusicList.toolbar.setTitle(R.string.toolbar_title_artist);
                        } else {
                            moveTaskToBack(true);
                        }
                        break;
                    case 3:
                        if (actionMode != null && AlbumListFragment.detailFragment == null) {
                            actionMode = null;
                            AlbumListFragment.positionSet.clear();
                            sendBroadcast(in);
                        } else if (actionMode != null && AlbumListFragment.detailFragment != null) {
                            actionMode = null;
                            AlbumDetailFragment.positionSet.clear();
                            getWindow().setStatusBarColor(0);
                            toolbar.setBackgroundColor(0);
                            sendBroadcast(in);
                        } else if (AlbumListFragment.detailFragment != null) {
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

                            MusicList.toolbar.setVisibility(View.VISIBLE);
                            MusicList.toolbar.setTitle(R.string.toolbar_title_album);
                        } else {
                            moveTaskToBack(true);
                        }
                        break;
                    case 4:
                        if (PlaylistFragment.playlistDetailFragment != null) {
                            if (PlaylistDetailFragment.isChange) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
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
                                    }
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
                            MusicList.toolbar.setVisibility(View.VISIBLE);
                            MusicList.toolbar.setTitle(R.string.toolbar_title_playlist);
                            PlaylistFragment.playlistDetailFragment = null;
                        } else {
                            moveTaskToBack(true);
                        }
                        break;
                    case 5:
                        if (actionMode != null) {
                            actionMode = null;
                            MusicRecentAddedFragment.positionSet.clear();
                            sendBroadcast(in);
                        } else {
                            moveTaskToBack(true);
                        }
                        break;
                    case 6:
                        if (actionMode != null && FolderFragment.folderDetailFragment != null) {
                            actionMode = null;
                            FolderDetailFragment.positionSet.clear();
                            getWindow().setStatusBarColor(0);
                            toolbar.setBackgroundColor(0);
                            sendBroadcast(in);
                        } else if (FolderFragment.folderDetailFragment != null) {
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

                            MusicList.toolbar.setVisibility(View.VISIBLE);
                            MusicList.toolbar.setTitle(R.string.toolbar_title_folder);
                        } else {
                            moveTaskToBack(true);
                        }
                        break;
                    default:
                        moveTaskToBack(true);
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
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        getWindow().setStatusBarColor(statusBarColor);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        MusicListFragment.positionSet.clear();
        sendBroadcast(new Intent("notifyDataSetChanged"));
        getWindow().setStatusBarColor(0);
    }


    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        Intent intent = new Intent("notifyDataSetChanged");
        switch (item.getItemId()) {
            case R.id.select_all:
                switch (Mod) {
                    case 1:
                        listPositionSet.clear();
                        musicUtils.selectAll(listPositionSet, MusicListFragment.musicList);
                        isArtist = false;
                        break;
                    case 2:
                        listPositionSet.clear();

                        if (ArtistListFragment.artistDetailFragment != null) {
                            musicUtils.selectAll(listPositionSet, ArtistDetailFragment.musicList);
                            isArtist = false;
                        } else {
                            musicUtils.selectAll(listPositionSet, ArtistListFragment.artistlist);
                            isArtist = true;
                        }
                        break;
                    case 3:
                        listPositionSet.clear();
                        if (AlbumListFragment.detailFragment != null) {
                            musicUtils.selectAll(listPositionSet, AlbumDetailFragment.musicList);
                            isArtist = false;
                        } else {
                            musicUtils.selectAll(listPositionSet, AlbumListFragment.albumlist);
                            isAlbum = true;
                        }
                        break;
                    case 5:
                        listPositionSet.clear();
                        musicUtils.selectAll(listPositionSet, MusicRecentAddedFragment.musicList);
                        isArtist = false;
                        break;
                    case 6:
                        listPositionSet.clear();
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
                        if (ArtistListFragment.artistDetailFragment != null) {
                            if (!MusicUtils.isSelectAll) {
                                listPositionSet = ArtistDetailFragment.positionSet;
                            }
                            musicList = ArtistDetailFragment.musicList;
                            isArtist = false;
                        } else {
                            if (!MusicUtils.isSelectAll) {
                                listPositionSet = ArtistListFragment.positionSet;
                            }
                            musicList = ArtistListFragment.artistlist;
                            isArtist = true;
                        }
                        break;
                    case 3:
                        if (AlbumListFragment.detailFragment != null) {

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
                Log.d("send", "onReceive: " + MusicListFragment.pos);
            } else if (Mod == 5) {
                MusicRecentAddedFragment.pos = intent.getIntExtra("position", 0);
                Log.d("send", "onReceive: " + MusicRecentAddedFragment.pos);
            }
        }
    }

    private final BroadcastReceiver musicFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "Music play to the end":
                    if (MusicUtils.enableDefaultCover) {
                        Glide.with(MusicList.this)
                                .load(R.drawable.default_album_art)
                                .error(R.drawable.default_album_art)
                                .into(mainImageView);
                    } else {

                        Glide.with(MusicList.this)
                                .load(MusicService.music.getAlbumArtUri())
                                .crossFade(500)
                                .error(R.drawable.default_album_art)
                                .into(mainImageView);
                    }
                    break;
                case "ActionModeChanged":
                    actionMode = startSupportActionMode(MusicList.this);
                    break;
                case "FLAG RESET TO 0":
                    flag = 0;
                    PlayOrPause.setImageResource(R.drawable.footplaywhite);
                    break;
                case "search_play":

                    MusicList.footTitle.setText(MusicService.music.getTitle());
                    MusicList.footArtist.setText(MusicService.music.getArtist());
                    MusicList.PlayOrPause.setImageResource(R.drawable.footpausewhite);

                    Glide.with(getApplicationContext())
                            .load(MusicService.music.getAlbumArtUri())
                            .asBitmap()
                            .centerCrop()
                            .placeholder(R.drawable.default_album_art_land)
                            .into(MusicList.accountHeader.getHeaderBackgroundView());
                    Glide.with(getApplicationContext())
                            .load(MusicService.music.getAlbumArtUri())
                            .placeholder(R.drawable.default_album_art)
                            .into(MusicList.footAlbumArt);
                    handler.post(runnable);
                    break;
                case "PlAYORPAUSE":
                    if (!MusicService.mediaPlayer.isPlaying()) {
                        if (MusicService.music == null) {
                            startMusic(MusicListFragment.pos);

                        }
                    }
                    break;
                case "select_file":

                    File file = new File(intent.getStringExtra("uri"));

                    lyricView.loadLrc(file);
                    handler.post(runnable1);

                    int bytesum = 0;
                    int byteread = 0;

                    String newSongTitle, newSinger;
                    if (MusicService.music.getTitle().contains("/")) {
                        newSongTitle = MusicService.music.getTitle().replace("/", "_");
                    } else {
                        newSongTitle = MusicService.music.getTitle();
                    }
                    if (MusicService.music.getArtist().contains("/")) {
                        newSinger = MusicService.music.getArtist().replace("/", "_");
                    } else {
                        newSinger = MusicService.music.getArtist();
                    }

                    String newPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/MusicPlayer/lyric" + "/" + newSongTitle + "_" + newSinger + ".lrc";
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
                    break;
            }
        }
    };

    private static IntentFilter musicFinishFilter() {
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("Music play to the end");
        intentfilter.addAction("ActionModeChanged");
        intentfilter.addAction("FLAG RESET TO 0");
        intentfilter.addAction("search_play");
        intentfilter.addAction("PlAYORPAUSE");
        intentfilter.addAction("select_file");
        return intentfilter;
    }

    private void findView() {
        slideView = findViewById(R.id.slideview);
        musicProgressBar = (ProgressBar) findViewById(R.id.music_progressbar);
        slideMenu = (ImageView) findViewById(R.id.slide_menu);
        footBar = (RelativeLayout) findViewById(R.id.footBar);
        play_pause = (FloatingActionButton) findViewById(R.id.music_play);
        ImageView next = (ImageView) findViewById(R.id.music_next);
        ImageView last = (ImageView) findViewById(R.id.music_last);
        title = (TextView) findViewById(R.id.title);
        artist = (TextView) findViewById(R.id.artist);
        album = (TextView) findViewById(R.id.album);
        mainImageView = (SquareImageView) findViewById(R.id.album_art);
        footTitle = (TextView) findViewById(R.id.footTitle);
        footArtist = (TextView) findViewById(R.id.footArtist);
        footAlbumArt = (ImageView) findViewById(R.id.footAlbumArt);
        PlayOrPause = (FloatingActionButton) findViewById(R.id.footPlayOrPause);
        duration = (TextView) findViewById(R.id.duration);
        current = (TextView) findViewById(R.id.current);
        musicProgress = (SeekBar) findViewById(R.id.music_progress);
        randomPlay = (ImageView) findViewById(R.id.random_play);
        cyclePlay = (ImageView) findViewById(R.id.cycle_play);
        lyricView = (LrcView) findViewById(R.id.lyric_view);
        lyricButton = (ImageView) findViewById(R.id.lyric);
        blurBG = (SquareImageView) findViewById(R.id.album_art_blur);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        PlayOrPause.setOnClickListener(this);
        slideMenu.setOnClickListener(this);
        play_pause.setOnClickListener(this);
        next.setOnClickListener(this);
        last.setOnClickListener(this);
        randomPlay.setOnClickListener(this);
        cyclePlay.setOnClickListener(this);
        lyricButton.setOnClickListener(this);
        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();

        footBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    xDown = event.getX();
                    yDown = event.getY();

                } else if (event.getAction() == MotionEvent.ACTION_UP) {// 松开处理

                    xUp = event.getX();

                    if ((xUp - xDown) > 20) {
                        if (MusicListFragment.count == 1
                            || ArtistDetailFragment.count == 1
                            || AlbumDetailFragment.count == 1
                            || MusicRecentAddedFragment.count == 1
                            || PlaylistDetailFragment.count == 1
                            || FolderDetailFragment.count == 1
                            || MusicService.mediaPlayer.isPlaying()
                            || flag == 1)
                            musicService.preMusic();
                    } else if ((xUp - xDown) < -20) {
                        if (MusicListFragment.count == 1
                                || ArtistDetailFragment.count == 1
                                || AlbumDetailFragment.count == 1
                                || MusicRecentAddedFragment.count == 1
                                || PlaylistDetailFragment.count == 1
                                || FolderDetailFragment.count == 1
                                || MusicService.mediaPlayer.isPlaying()
                                || flag == 1)
                            musicService.nextMusic();
                    } else if (0 == (xDown - xUp)) {
                        playOrPauseMusic();
                    }
                }
                return true;
            }
        });
    }

}

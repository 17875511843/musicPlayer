package ironbear775.com.musicplayer.Activity;


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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
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
import ironbear775.com.musicplayer.Fragment.MusicListFragment;
import ironbear775.com.musicplayer.Fragment.MusicRecentAddedFragment;
import ironbear775.com.musicplayer.Fragment.PlaylistDetailFragment;
import ironbear775.com.musicplayer.Fragment.PlaylistFragment;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MusicService;
import ironbear775.com.musicplayer.Util.MusicUtils;
import ironbear775.com.musicplayer.Util.Notification;
import ironbear775.com.musicplayer.Util.PlaylistDbHelper;
import ironbear775.com.musicplayer.Util.PlaylistDialog;
import ironbear775.com.musicplayer.Util.SquareImageView;

/**
 * Created by ironbear on 2016/12/9.
 */

public class MusicList extends BaseActivity implements Serializable, View.OnClickListener
        , ActionMode.Callback {
    public static TextView footTitle;
    public static TextView footArtist;
    public static ImageView footAlbumArt;
    public static android.support.design.widget.FloatingActionButton PlayOrPause;
    private FloatingActionButton play_pause;
    public static AccountHeader accountHeader;
    public static Fragment albumFragment;
    public static Fragment artistFragment;
    public static Fragment playlistFragment;
    public static int Mod = 1;
    public static ActionMode actionMode;
    public static boolean isAlbum = false;
    public static boolean isArtist = false;
    public static int flag = 0;

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
    private Drawer slideDrawer;
    private ProgressBar musicProgressBar;
    private android.app.Fragment musicFragment, musicRecentFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private ArrayList<Music> musicList = new ArrayList<>();
    private Set<Integer> listPositionSet = new HashSet<>();
    private MusicUtils musicUtils;
    private View slideView;
    private Notification noti;
    public static int statusBarColor;
    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MusicBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_list_layout);

        findView();

        init();

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
        MusicUtils.enableDownload = sharedPreferences.getBoolean("enableDownload", true);
        MusicUtils.enableDefaultCover = sharedPreferences.getBoolean("enableDefaultCover", false);
        MusicUtils.enableColorNotification = sharedPreferences.getBoolean("enableColorNotification", false);
        MusicUtils.enableLockscreenNotification = sharedPreferences.getBoolean("enableLockscreenNotification", true);
        MusicUtils.launchPage = sharedPreferences.getInt("launchPage", 1);

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

                            PlayOrPause.show();
                            Intent intent = new Intent("SetClickable_True");
                            sendBroadcast(intent);

                            footBar.setVisibility(View.VISIBLE);
                            footBar.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .build();
    }

    private void showFragment(int i) {
        transaction = fragmentManager.beginTransaction();
        if (musicRecentFragment != null) {
            transaction.hide(musicRecentFragment);
        }
        if (albumFragment != null) {
            transaction.hide(albumFragment);
        }
        if (musicFragment != null) {
            transaction.hide(musicFragment);
        }
        if (AlbumListFragment.detailFragment != null) {
            transaction.hide(AlbumListFragment.detailFragment);
        }
        if (artistFragment != null) {
            transaction.hide(artistFragment);
        }
        if (playlistFragment != null) {
            transaction.hide(playlistFragment);
        }
        if (ArtistListFragment.artistDetailFragment != null) {
            transaction.hide(ArtistListFragment.artistDetailFragment);
        }
        if (PlaylistFragment.playlistDetailFragment != null) {
            transaction.hide(PlaylistFragment.playlistDetailFragment);
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

        SecondaryDrawerItem slideSleepTimer = new SecondaryDrawerItem()
                .withIdentifier(6)
                .withIcon(R.drawable.sleeptimer)
                .withSelectedTextColorRes(R.color.material_gray_dark)
                .withName(getResources().getString(R.string.sleep_timer));

        SecondaryDrawerItem setting = new SecondaryDrawerItem()
                .withIdentifier(7)
                .withIcon(R.drawable.settings)
                .withSelectedTextColorRes(R.color.material_gray_dark)
                .withName(getResources().getString(R.string.settings));

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
                        slideRecentAdded,
                        new DividerDrawerItem(),
                        slideSleepTimer,
                        setting

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
                                Intent timerIntent = new Intent(MusicList.this, SleepTimer.class);
                                startActivity(timerIntent);
                                break;
                            case 7:
                                Intent settingIntent = new Intent(MusicList.this, Setting.class);
                                startActivity(settingIntent);
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
            case R.id.footLayout:
                //startMusicIntent();
                if (MusicListFragment.musicList.size() > 0
                        || MusicRecentAddedFragment.musicList.size() > 0
                        || ArtistListFragment.artistlist.size() > 0
                        || AlbumListFragment.albumlist.size() > 0
                        || PlaylistDetailFragment.musicList.size() > 0) {
                    playOrPauseMusic();
                }
                break;
            case R.id.footAlbumArt:
                //startMusicIntent();
                if (MusicListFragment.musicList.size() > 0
                        || MusicRecentAddedFragment.musicList.size() > 0
                        || ArtistListFragment.artistlist.size() > 0
                        || AlbumListFragment.albumlist.size() > 0
                        || PlaylistDetailFragment.musicList.size() > 0) {
                    playOrPauseMusic();
                }
                break;
            case R.id.footPlayOrPause:
                if ((MusicListFragment.musicList.size() > 0
                        || MusicRecentAddedFragment.musicList.size() > 0
                        || ArtistListFragment.artistlist.size() > 0
                        || AlbumListFragment.albumlist.size() > 0
                        || PlaylistDetailFragment.musicList.size() > 0)) {
                    if (MusicService.mediaPlayer.isPlaying()) {
                        MusicService.mediaPlayer.pause();
                        PlayOrPause.setImageResource(R.drawable.footplaywhite);
                        play_pause.setImageResource(R.drawable.footplaywhite);

                        createNotification(MusicService.music.getAlbumArtUri(), R.drawable.footplay);

                        MusicUtils.saveInfoService(getApplicationContext());
                    } else if (MusicListFragment.count == 0
                            && AlbumDetailFragment.count == 0
                            && MusicRecentAddedFragment.count == 0
                            && ArtistDetailFragment.count == 0
                            && PlaylistDetailFragment.count == 0
                            && !MusicService.mediaPlayer.isPlaying()
                            && flag == 0) {
                        startMusic(MusicUtils.pos);
                        PlayOrPause.setImageResource(R.drawable.footpausewhite);
                        play_pause.setImageResource(R.drawable.footpausewhite);

                    } else {
                        MusicService.mediaPlayer.start();
                        PlayOrPause.setImageResource(R.drawable.footpausewhite);
                        play_pause.setImageResource(R.drawable.footpausewhite);

                        createNotification(MusicService.music.getAlbumArtUri(), R.drawable.footpause);

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
            case R.id.slideDown:
                slideUp.hide();
                Intent intent = new Intent("SetClickable_True");
                sendBroadcast(intent);
                footBar.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void createNotification(final String str, final int id) {
        noti = new Notification(musicService);
        final Message msg = new Message();

        if (str != null) {
            Glide.with(getApplicationContext())
                    .load(str)
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
                                        noti.createNotification(getBaseContext(), id,
                                                MusicService.musicList, msg);
                                    } else {
                                        swatch = palette.getMutedSwatch();
                                        if (swatch != null) {
                                            msg.what = swatch.getRgb();
                                            msg.arg1 = swatch.getTitleTextColor();
                                            msg.arg2 = swatch.getBodyTextColor();
                                            msg.obj = MusicUtils.messageGood;
                                            noti.createNotification(getBaseContext(), id,
                                                    MusicService.musicList, msg);
                                        } else {
                                            msg.obj = MusicUtils.messageBad;
                                            noti.createNotification(getBaseContext(), id,
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
                            noti.createNotification(getBaseContext(), id,
                                    MusicService.musicList, msg);
                        }
                    });
        } else {
            msg.obj = MusicUtils.messageNull;
            noti.createNotification(getBaseContext(), id,
                    MusicService.musicList, msg);
        }

    }

    private void playOrPauseMusic() {

        if (MusicListFragment.count == 0
                && ArtistDetailFragment.count == 0
                && AlbumDetailFragment.count == 0
                && MusicRecentAddedFragment.count == 0
                && MusicRecentAddedFragment.count == 0
                && PlaylistDetailFragment.count == 0
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
            PlayOrPause.setImageResource(R.drawable.footpausewhite);
            play_pause.setImageResource(R.drawable.footpausewhite);


            if (MusicListFragment.musicList.size() >= 1) {

                if (MusicUtils.enableDefaultCover) {
                    Glide.with(MusicList.this)
                            .load(R.drawable.default_album_art)
                            .error(R.drawable.default_album_art)
                            .into(mainImageView);
                } else {
                    createNotification(MusicListFragment.musicList.get(MusicListFragment.pos).getAlbumArtUri(), R.drawable.footpause);

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
                || flag == 1) {
            MusicService.mediaPlayer.start();
            initMusicPlayer();

            PlayOrPause.setImageResource(R.drawable.footpausewhite);
            play_pause.setImageResource(R.drawable.footpausewhite);

            if (MusicUtils.enableDefaultCover) {
                Glide.with(MusicList.this)
                        .load(R.drawable.default_album_art)
                        .error(R.drawable.default_album_art)
                        .into(mainImageView);
            } else {
                createNotification(MusicService.music.getAlbumArtUri(), R.drawable.footpause);

                Glide.with(MusicList.this)
                        .load(MusicService.music.getAlbumArtUri())
                        .crossFade(500)
                        .error(R.drawable.default_album_art)
                        .into(mainImageView);
            }
        }

        Intent intent = new Intent("SetClickable_False");
        sendBroadcast(intent);

        slideUp.show();
        PlayOrPause.hide();
        footBar.setVisibility(View.GONE);
    }

    //ListView和service绑定
    private void startMusic(int position) {

        Intent serviceIntent = new Intent(MusicList.this, MusicService.class);
        serviceIntent.setAction("musiclist");

       /* Bundle bundle = new Bundle();
        bundle.putInt("musicPosition", position);
        bundle.putInt("musicProgress", progress);*/
        if (MusicListFragment.count == 1) {
            serviceIntent.putParcelableArrayListExtra("musicList", MusicListFragment.musicList);

            //bundle.putParcelableArrayList("musicList", MusicListFragment.musicList);
        } else if (AlbumDetailFragment.count == 1) {
            serviceIntent.putParcelableArrayListExtra("musicList", AlbumDetailFragment.musicList);

            //bundle.putParcelableArrayList("musicList", AlbumDetailFragment.musicList);
        } else if (MusicRecentAddedFragment.count == 1) {
            serviceIntent.putParcelableArrayListExtra("musicList", MusicRecentAddedFragment.musicList);

            //bundle.putParcelableArrayList("musicList", MusicRecentAddedFragment.musicList);
        } else if (ArtistDetailFragment.count == 1) {
            serviceIntent.putParcelableArrayListExtra("musicList", ArtistDetailFragment.musicList);

            //bundle.putParcelableArrayList("musicList", ArtistDetailFragment.musicList);
        } else if (PlaylistDetailFragment.count == 1) {
            serviceIntent.putParcelableArrayListExtra("musicList", PlaylistDetailFragment.musicList);

            //bundle.putParcelableArrayList("musicList", PlaylistDetailFragment.musicList);
        } else {
            serviceIntent.putParcelableArrayListExtra("musicList", MusicListFragment.musicList);

            //bundle.putParcelableArrayList("musicList", MusicListFragment.musicList);
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
    }

    private final Handler handler = new Handler();
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

                    startMusic(MusicListFragment.pos);
                    MusicService.isRandom = true;
                    MusicList.footTitle.setText(MusicListFragment.musicList.get(MusicListFragment.pos).getTitle());
                    MusicList.footArtist.setText(MusicListFragment.musicList.get(MusicListFragment.pos).getArtist());
                    MusicList.PlayOrPause.setImageResource(R.drawable.footpausewhite);
                    getFootAlbumArt(MusicListFragment.pos);

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

                    startMusic(MusicRecentAddedFragment.pos);
                    MusicService.isRandom = true;
                    MusicList.footTitle.setText(MusicRecentAddedFragment.musicList.get(MusicRecentAddedFragment.pos).getTitle());
                    MusicList.footArtist.setText(MusicRecentAddedFragment.musicList.get(MusicRecentAddedFragment.pos).getArtist());
                    MusicList.PlayOrPause.setImageResource(R.drawable.footpausewhite);
                    getFootAlbumArt(MusicRecentAddedFragment.pos);

                }
                break;
        }
    }

    //设置底部栏专辑封面
    private void getFootAlbumArt(int pos1) {
        String AlbumUri = null;
        switch (Mod) {
            case 1:
                AlbumUri = MusicListFragment.musicList.get(pos1).getAlbumArtUri();
                break;
            case 2:
                AlbumUri = ArtistDetailFragment.musicList.get(pos1).getAlbumArtUri();
                break;
            case 3:
                AlbumUri = AlbumDetailFragment.musicList.get(pos1).getAlbumArtUri();
                break;
            case 5:
                AlbumUri = MusicRecentAddedFragment.musicList.get(pos1).getAlbumArtUri();
                break;

        }
        Glide.with(this)
                .load(AlbumUri)
                .centerCrop()
                .error(R.drawable.default_album_art)
                .placeholder(R.drawable.default_album_art)
                .into(accountHeader.getHeaderBackgroundView());
        Glide.with(this)
                .load(AlbumUri)
                .centerCrop()
                .error(R.drawable.default_album_art)
                .placeholder(R.drawable.default_album_art)
                .into(footAlbumArt);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.getItem(0);
        if (Mod == 2 && ArtistListFragment.artistDetailFragment == null
                || Mod == 3 && AlbumListFragment.detailFragment == null
                || Mod == 4) {
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
                return true;
        }
        return super.onOptionsItemSelected(item);
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

                footBar.setVisibility(View.VISIBLE);
                footBar.setVisibility(View.VISIBLE);
            } else {
                Intent in = new Intent("notifyDataSetChanged");
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
                            sendBroadcast(in);
                        } else if (ArtistListFragment.artistDetailFragment != null) {
                            transaction = fragmentManager.beginTransaction();
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
                            sendBroadcast(in);
                        } else if (AlbumListFragment.detailFragment != null) {
                            transaction = fragmentManager.beginTransaction();
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
                            transaction.hide(PlaylistFragment.playlistDetailFragment);
                            transaction.setCustomAnimations(
                                    R.animator.fragment_slide_right_enter,
                                    R.animator.fragment_slide_right_exit,
                                    R.animator.fragment_slide_left_enter,
                                    R.animator.fragment_slide_left_exit
                            );
                            transaction.show(playlistFragment);
                            transaction.commit();
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
        Intent in = new Intent("notifyDataSetChanged");
        sendBroadcast(in);
        getWindow().setStatusBarColor(0);
    }


    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_to_list:
                switch (Mod) {
                    case 1:
                        listPositionSet = MusicListFragment.positionSet;
                        musicList = MusicListFragment.musicList;
                        isArtist = false;
                        break;
                    case 2:
                        if (ArtistListFragment.artistDetailFragment != null) {
                            listPositionSet = ArtistDetailFragment.positionSet;
                            musicList = ArtistDetailFragment.musicList;
                            isArtist = false;
                        } else {
                            listPositionSet = ArtistListFragment.positionSet;
                            musicList = ArtistListFragment.artistlist;
                            isArtist = true;
                        }
                        break;
                    case 3:
                        if (AlbumListFragment.detailFragment != null) {
                            listPositionSet = AlbumDetailFragment.positionSet;
                            musicList = AlbumDetailFragment.musicList;
                            isArtist = false;
                        } else {
                            listPositionSet = AlbumListFragment.positionSet;
                            musicList = AlbumListFragment.albumlist;
                            isAlbum = true;
                        }
                        break;
                    case 5:
                        listPositionSet = MusicRecentAddedFragment.positionSet;
                        musicList = MusicRecentAddedFragment.musicList;
                        isArtist = false;
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
                        createNotification(MusicService.music.getAlbumArtUri(), R.drawable.footpause);

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
            }
        }
    };

    private static IntentFilter musicFinishFilter() {
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("Music play to the end");
        intentfilter.addAction("ActionModeChanged");
        intentfilter.addAction("FLAG RESET TO 0");
        return intentfilter;
    }

    private void findView() {
        slideView = findViewById(R.id.slideview);
        musicProgressBar = (ProgressBar) findViewById(R.id.music_progressbar);
        ImageView slideDown = (ImageView) findViewById(R.id.slideDown);
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

        RelativeLayout footLayout = (RelativeLayout) findViewById(R.id.footLayout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        footLayout.setOnClickListener(this);
        footAlbumArt.setOnClickListener(this);
        PlayOrPause.setOnClickListener(this);
        slideDown.setOnClickListener(this);
        play_pause.setOnClickListener(this);
        next.setOnClickListener(this);
        last.setOnClickListener(this);
        randomPlay.setOnClickListener(this);
        cyclePlay.setOnClickListener(this);

        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();

    }
}

package ironbear775.com.musicplayer.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MusicService;
import ironbear775.com.musicplayer.Util.ColorPickerDialog;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2017/3/28.
 */

public class Setting extends BaseActivity implements Switch.OnCheckedChangeListener
        , View.OnClickListener {
    private Toolbar toolbar;
    private String[] time, enableDownload, lyric, startPage;
    private TextView filter;
    private TextView cache;
    private Dialog dialog;
    private TextView albumText;
    private TextView artistText;
    private TextView lyricText;
    private TextView startText;
    private RestartReceiver restartReceiver;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setting_layout);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("restart yourself");

        restartReceiver = new RestartReceiver();

        registerReceiver(restartReceiver, intentFilter);

        startPage = new String[]{getResources().getString(R.string.slideBar_title_music),
                getResources().getString(R.string.toolbar_title_artist),
                getResources().getString(R.string.toolbar_title_album),
                getResources().getString(R.string.toolbar_title_playlist),
                getResources().getString(R.string.toolbar_title_recent_added),
                getResources().getString(R.string.toolbar_title_folder)};
        time = new String[]{"0s", "15s", "20s", "30s", "40s", "50s", "60s"};

        enableDownload = new String[]{getResources().getString(R.string.never),
                getResources().getString(R.string.always),
                getResources().getString(R.string.only_wifi)};

        lyric = new String[]{getResources().getString(R.string.netease_first), getResources().getString(R.string.netease),
                getResources().getString(R.string.kugou)};

        findViews();

        toolbar.setTitle(getResources().getString(R.string.settings));
        toolbar.setTitleTextColor(Color.WHITE);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void findViews() {

        RelativeLayout clearCache = findViewById(R.id.layout_cache);
        RelativeLayout colorPicker = findViewById(R.id.color_picker);
        filter = findViewById(R.id.filter_text);
        toolbar = findViewById(R.id.toolbar);
        Switch defaultAlbumArt = findViewById(R.id.use_default_cover);
        Switch colorNotification = findViewById(R.id.color_notification);
        Switch oldStyleNotification = findViewById(R.id.use_old_notification);
        Switch keepScreenOn = findViewById(R.id.keep_screen_on);
        Switch loadWebLyric = findViewById(R.id.load_web_lyric);
        Switch enableShuffle = findViewById(R.id.enable_shuffle);
        Switch enableSwipe = findViewById(R.id.swipe_gesture);
        Switch enableTranslateLyric = findViewById(R.id.load_translate_lyric);
        Switch autoSwitchNightMode = findViewById(R.id.auto_night_mode);
        Switch changeMainWindow = findViewById(R.id.change_main_window);
        cache = findViewById(R.id.clear_cache);
        TextView equalizer = findViewById(R.id.equalizer);
        TextView about = findViewById(R.id.about);
        RelativeLayout matchArtistCover = findViewById(R.id.match_artist_layout);
        RelativeLayout matchAlbumCover = findViewById(R.id.match_album_layout);
        RelativeLayout lyricLayout = findViewById(R.id.lyric_layout);
        artistText = findViewById(R.id.match_artist);
        albumText = findViewById(R.id.match_album);
        lyricText = findViewById(R.id.lyric_source);
        startText = findViewById(R.id.start_page);

        cache.setText(MusicUtils.getInstance().getCacheSize(this));

        if (BaseActivity.isNight)
            filter.setTextColor(getResources().getColor(R.color.nightMainTextColor));
        else
            filter.setTextColor(getResources().getColor(R.color.lightMainTextColor));
        String t;
        switch (MusicUtils.getInstance().filterNum) {
            case 0:
                t = getResources().getString(R.string.ignore) + " 0 " + getResources().getString(R.string.second);
                filter.setText(t);
                break;
            case 1:
                t = getResources().getString(R.string.ignore) + " 15 " + getResources().getString(R.string.second);
                filter.setText(t);
                break;
            case 2:
                t = getResources().getString(R.string.ignore) + " 20 " + getResources().getString(R.string.second);
                filter.setText(t);
                break;
            case 3:
                t = getResources().getString(R.string.ignore) + " 30 " + getResources().getString(R.string.second);
                filter.setText(t);
                break;
            case 4:
                t = getResources().getString(R.string.ignore) + " 40 " + getResources().getString(R.string.second);
                filter.setText(t);
                break;
            case 5:
                t = getResources().getString(R.string.ignore) + " 50 " + getResources().getString(R.string.second);
                filter.setText(t);
                break;
            case 6:
                t = getResources().getString(R.string.ignore) + " 60 " + getResources().getString(R.string.second);
                filter.setText(t);
                break;
        }

        switch (MusicUtils.getInstance().downloadArtist) {
            case 0:
                artistText.setText(getResources().getString(R.string.never));
                break;
            case 1:
                artistText.setText(getResources().getString(R.string.always));
                break;
            case 2:
                artistText.setText(getResources().getString(R.string.only_wifi));
                break;

        }
        switch (MusicUtils.getInstance().downloadAlbum) {
            case 0:
                albumText.setText(getResources().getString(R.string.never));
                break;
            case 1:
                albumText.setText(getResources().getString(R.string.always));
                break;
            case 2:
                albumText.setText(getResources().getString(R.string.only_wifi));
                break;

        }
        switch (MusicUtils.getInstance().loadlyric) {
            case 0:
                lyricText.setText(getResources().getString(R.string.netease_first_1));
                break;
            case 1:
                lyricText.setText(getResources().getString(R.string.only_netease));
                break;
            case 2:
                lyricText.setText(getResources().getString(R.string.only_Kugou));
                break;
        }
        switch (MusicUtils.getInstance().launchPage) {
            case 1:
                startText.setText(R.string.slideBar_title_music);
                break;
            case 2:
                startText.setText(R.string.toolbar_title_artist);
                break;
            case 3:
                startText.setText(R.string.toolbar_title_album);
                break;
            case 4:
                startText.setText(R.string.toolbar_title_playlist);
                break;
            case 5:
                startText.setText(R.string.toolbar_title_recent_added);
                break;
            case 6:
                startText.setText(R.string.toolbar_title_folder);
                break;
        }

        defaultAlbumArt.setChecked(MusicUtils.getInstance().enableDefaultCover);
        colorNotification.setChecked(MusicUtils.getInstance().enableColorNotification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            oldStyleNotification.setChecked(MusicUtils.getInstance().useOldStyleNotification);
        } else {
            oldStyleNotification.setVisibility(View.GONE);
        }

        keepScreenOn.setChecked(MusicUtils.getInstance().keepScreenOn);
        loadWebLyric.setChecked(MusicUtils.getInstance().loadWebLyric);
        enableShuffle.setChecked(MusicUtils.getInstance().enableShuffle);
        enableSwipe.setChecked(MusicUtils.getInstance().enableSwipeGesture);
        enableTranslateLyric.setChecked(MusicUtils.getInstance().enableTranslateLyric);
        autoSwitchNightMode.setChecked(MusicUtils.getInstance().autoSwitchNightMode);
        changeMainWindow.setChecked(BaseActivity.changeMainWindow);

        defaultAlbumArt.setOnCheckedChangeListener(this);
        colorNotification.setOnCheckedChangeListener(this);
        oldStyleNotification.setOnCheckedChangeListener(this);
        keepScreenOn.setOnCheckedChangeListener(this);
        loadWebLyric.setOnCheckedChangeListener(this);
        enableShuffle.setOnCheckedChangeListener(this);
        enableSwipe.setOnCheckedChangeListener(this);
        enableTranslateLyric.setOnCheckedChangeListener(this);
        autoSwitchNightMode.setOnCheckedChangeListener(this);
        changeMainWindow.setOnCheckedChangeListener(this);

        equalizer.setOnClickListener(this);
        filter.setOnClickListener(this);
        about.setOnClickListener(this);
        clearCache.setOnClickListener(this);
        colorPicker.setOnClickListener(this);
        matchArtistCover.setOnClickListener(this);
        matchAlbumCover.setOnClickListener(this);
        lyricLayout.setOnClickListener(this);
        startText.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        switch (buttonView.getId()) {
            case R.id.use_default_cover:
                MusicUtils.getInstance().enableDefaultCover = isChecked;
                editor.putBoolean("enableDefaultCover", MusicUtils.getInstance().enableDefaultCover);
                editor.apply();
                Intent intent2 = new Intent("enableDefaultCover");
                sendBroadcast(intent2);
                break;
            case R.id.color_notification:
                MusicUtils.getInstance().enableColorNotification = isChecked;
                editor.putBoolean("enableColorNotification", MusicUtils.getInstance().enableColorNotification);
                editor.apply();
                Intent intent3 = new Intent("enableColorNotification");
                sendBroadcast(intent3);
                break;
            case R.id.use_old_notification:
                MusicUtils.getInstance().useOldStyleNotification = isChecked;
                editor.putBoolean("useOldStyleNotification", MusicUtils.getInstance().useOldStyleNotification);
                break;
            case R.id.keep_screen_on:
                MusicUtils.getInstance().keepScreenOn = isChecked;
                editor.putBoolean("keepScreenOn", MusicUtils.getInstance().keepScreenOn);
                editor.apply();
                sendBroadcast(new Intent("keep screen on"));
                break;
            case R.id.load_web_lyric:
                MusicUtils.getInstance().loadWebLyric = isChecked;
                editor.putBoolean("loadWebLyric", MusicUtils.getInstance().loadWebLyric);
                editor.apply();
                break;
            case R.id.enable_shuffle:
                MusicUtils.getInstance().enableShuffle = isChecked;
                editor.putBoolean("enableShuffle", MusicUtils.getInstance().enableShuffle);
                editor.apply();
                Intent intent5 = new Intent("enableShuffle");
                sendBroadcast(intent5);
                break;
            case R.id.swipe_gesture:
                MusicUtils.getInstance().enableSwipeGesture = isChecked;
                editor.putBoolean("enableSwipeGesture", MusicUtils.getInstance().enableSwipeGesture);
                editor.apply();
                break;
            case R.id.load_translate_lyric:
                MusicUtils.getInstance().enableTranslateLyric = isChecked;
                editor.putBoolean("enableTranslateLyric", MusicUtils.getInstance().enableTranslateLyric);
                editor.apply();
                break;
            case R.id.auto_night_mode:
                MusicUtils.getInstance().autoSwitchNightMode = isChecked;
                editor.putBoolean("autoSwitchNightMode", MusicUtils.getInstance().autoSwitchNightMode);
                editor.apply();
                break;
            case R.id.change_main_window:
                BaseActivity.changeMainWindow = isChecked;
                editor.putBoolean("changeMainWindow", BaseActivity.changeMainWindow);
                editor.apply();
                sendBroadcast(new Intent("restart yourself"));
                break;
        }
        editor.commit();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 11) {

                dialog = ProgressDialog.show(Setting.this, null, getResources().getString(R.string.clear_cache));

                new Thread(() -> {
                    MusicUtils.getInstance().clearImageAllCache(getApplicationContext());
                    if (MusicUtils.getInstance().getCacheSize(getApplicationContext()).equals("0.0Byte")) {
                        Message message = new Message();
                        message.what = 13;
                        handler.sendMessage(message);
                    }
                }).start();
            }
            if (msg.what == 13 && dialog.isShowing()) {
                dialog.dismiss();
            }
            cache.setText(MusicUtils.getInstance().getCacheSize(getApplicationContext()));
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about:
                Intent intent = new Intent(this, About.class);
                startActivity(intent);
                break;
            case R.id.layout_cache:
                AlertDialog.Builder clearAlert = new AlertDialog.Builder(Setting.this);
                clearAlert.setTitle(R.string.clear_cache_judge);
                clearAlert.setCancelable(true);
                clearAlert.setNegativeButton(R.string.no, (dialog, which) -> {

                });

                clearAlert.setPositiveButton(R.string.yes, (dialog, which) -> {
                    Message message = new Message();
                    message.what = 11;
                    handler.sendMessage(message);
                });
                clearAlert.show();
                break;
            case R.id.filter_text:
                AlertDialog.Builder filterDialog;
                if (!isNight)
                    filterDialog = new AlertDialog
                            .Builder(this, R.style.MaterialThemeDialog);
                else
                    filterDialog = new AlertDialog.Builder(this);

                filterDialog.setTitle("Ignore");
                filterDialog.setSingleChoiceItems(time, MusicUtils.getInstance().filterNum, (dialogInterface, i) -> {
                    MusicUtils.getInstance().filterNum = i;

                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putInt("filterNum", MusicUtils.getInstance().filterNum);
                    editor.apply();
                    if (BaseActivity.isNight)
                        filter.setTextColor(getResources().getColor(R.color.nightMainTextColor));
                    else
                        filter.setTextColor(getResources().getColor(R.color.lightMainTextColor));
                    String t;
                    switch (MusicUtils.getInstance().filterNum) {
                        case 0:
                            t = getResources().getString(R.string.ignore) + " 0 " + getResources().getString(R.string.second);
                            filter.setText(t);
                            break;
                        case 1:
                            t = getResources().getString(R.string.ignore) + " 15 " + getResources().getString(R.string.second);
                            filter.setText(t);
                            break;
                        case 2:
                            t = getResources().getString(R.string.ignore) + " 20 " + getResources().getString(R.string.second);
                            filter.setText(t);
                            break;
                        case 3:
                            t = getResources().getString(R.string.ignore) + " 30 " + getResources().getString(R.string.second);
                            filter.setText(t);
                            break;
                        case 4:
                            t = getResources().getString(R.string.ignore) + " 40 " + getResources().getString(R.string.second);
                            filter.setText(t);
                            break;
                        case 5:
                            t = getResources().getString(R.string.ignore) + " 50 " + getResources().getString(R.string.second);
                            filter.setText(t);
                            break;
                        case 6:
                            t = getResources().getString(R.string.ignore) + " 60 " + getResources().getString(R.string.second);
                            filter.setText(t);
                            break;
                    }
                    dialogInterface.cancel();
                });
                filterDialog.show();
                break;
            case R.id.equalizer:
                Intent in = new Intent(AudioEffect
                        .ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);

                if ((in.resolveActivity(getPackageManager()) != null)) {
                    startActivityForResult(in, 0);
                } else {
                    if (!MusicService.isBluetoothHeadsetConnected) {
                        Intent i = new Intent(getApplicationContext(), Equalizer.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(Setting.this, R.string.equalizer_cant_open,
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.match_artist_layout:
                AlertDialog.Builder artistDialog;
                if (!isNight)
                    artistDialog = new AlertDialog
                            .Builder(this, R.style.MaterialThemeDialog);
                else
                    artistDialog = new AlertDialog.Builder(this);

                artistDialog.setTitle(R.string.match_artist_cover);
                artistDialog.setSingleChoiceItems(enableDownload, MusicUtils.getInstance().downloadArtist, (dialogInterface, i) -> {
                    MusicUtils.getInstance().downloadArtist = i;

                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putInt("downloadArtist", MusicUtils.getInstance().downloadArtist);
                    editor.apply();
                    switch (MusicUtils.getInstance().downloadArtist) {
                        case 0:
                            artistText.setText(getResources().getString(R.string.never));
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Setting.this);
                            alertDialog.setTitle(R.string.delete_download_image);
                            alertDialog.setCancelable(true);
                            alertDialog.setNegativeButton(R.string.no, (dialog13, which13) -> {

                            });
                            alertDialog.setPositiveButton(R.string.yes, (dialog14, which14) -> {
                                File file = new File(MusicUtils.getInstance().localPath, MusicUtils.getInstance().artistFolder);
                                MusicUtils.getInstance().deleteDownloadImage(file);

                            });
                            alertDialog.show();
                            break;
                        case 1:
                            artistText.setText(getResources().getString(R.string.always));
                            break;
                        case 2:
                            artistText.setText(getResources().getString(R.string.only_wifi));
                            break;

                    }
                    dialogInterface.cancel();
                });

                artistDialog.show();
                break;

            case R.id.start_page:
                AlertDialog.Builder pageDialog;
                if (!isNight)
                    pageDialog = new AlertDialog.Builder(this,
                            R.style.MaterialThemeDialog);
                else
                    pageDialog = new AlertDialog.Builder(this);
                pageDialog.setTitle(R.string.start_page);
                pageDialog.setSingleChoiceItems(startPage, MusicUtils.getInstance().launchPage-1, (dialogInterface, i) -> {

                    Log.d("TAG", "onClick: "+i);
                    switch (i+1) {
                        case 1:
                            startText.setText(R.string.slideBar_title_music);
                            break;
                        case 2:
                            startText.setText(R.string.toolbar_title_artist);
                            break;
                        case 3:
                            startText.setText(R.string.toolbar_title_album);
                            break;
                        case 4:
                            startText.setText(R.string.toolbar_title_playlist);
                            break;
                        case 5:
                            startText.setText(R.string.toolbar_title_recent_added);
                            break;
                        case 6:
                            startText.setText(R.string.toolbar_title_folder);
                            break;
                    }
                    MusicUtils.getInstance().launchPage = i+1;
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putInt("launchPage", MusicUtils.getInstance().launchPage);
                    editor.apply();
                    dialogInterface.cancel();
                });
                pageDialog.show();
                break;
            case R.id.match_album_layout:
                AlertDialog.Builder albumDialog;
                if (!isNight)
                    albumDialog = new AlertDialog
                            .Builder(this, R.style.MaterialThemeDialog);
                else
                    albumDialog = new AlertDialog.Builder(this);

                albumDialog.setTitle(R.string.match_album_cover);
                albumDialog.setSingleChoiceItems(enableDownload, MusicUtils.getInstance().downloadAlbum, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MusicUtils.getInstance().downloadAlbum = i;

                        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        editor.putInt("downloadAlbum", MusicUtils.getInstance().downloadAlbum);
                        editor.apply();
                        switch (MusicUtils.getInstance().downloadAlbum) {
                            case 0:
                                albumText.setText(getResources().getString(R.string.never));
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Setting.this);
                                alertDialog.setTitle(R.string.delete_download_image);
                                alertDialog.setCancelable(true);
                                alertDialog.setNegativeButton(R.string.no, (dialog1, which1) -> {

                                });
                                alertDialog.setPositiveButton(R.string.yes, (dialog12, which12) -> {
                                    File file = new File(MusicUtils.getInstance().localPath, MusicUtils.getInstance().albumFolder);
                                    MusicUtils.getInstance().deleteDownloadImage(file);
                                });
                                alertDialog.show();
                                break;
                            case 1:
                                albumText.setText(getResources().getString(R.string.always));
                                break;
                            case 2:
                                albumText.setText(getResources().getString(R.string.only_wifi));
                                break;
                        }
                        dialogInterface.cancel();
                    }
                });

                albumDialog.show();
                break;
            case R.id.lyric_layout:

                AlertDialog.Builder builder;
                if (!isNight)
                    builder = new AlertDialog
                            .Builder(this, R.style.MaterialThemeDialog);
                else
                    builder = new AlertDialog.Builder(this);

                builder.setTitle(R.string.update_lyric);
                builder.setSingleChoiceItems(lyric, MusicUtils.getInstance().loadlyric, (dialogInterface, i) -> {
                    MusicUtils.getInstance().loadlyric = i;
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putInt("loadlyric", MusicUtils.getInstance().loadlyric);
                    editor.apply();
                    switch (MusicUtils.getInstance().loadlyric) {
                        case 0:
                            lyricText.setText(getResources().getString(R.string.netease_first_1));
                            break;
                        case 1:
                            lyricText.setText(getResources().getString(R.string.only_netease));
                            break;
                        case 2:
                            lyricText.setText(getResources().getString(R.string.only_Kugou));
                            break;
                    }
                    dialogInterface.cancel();
                });
                builder.show();
                break;
            case R.id.color_picker:
                ColorPickerDialog dialog = new ColorPickerDialog(Setting.this);
                dialog.show();
                break;
        }
    }

    class RestartReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("restart yourself")) {
                finish();
                final Intent themeIntent = getIntent();
                themeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(themeIntent);
                overridePendingTransition(0, 0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(restartReceiver);
    }
}

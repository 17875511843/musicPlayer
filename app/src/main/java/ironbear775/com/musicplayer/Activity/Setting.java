package ironbear775.com.musicplayer.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;

import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2017/3/28.
 */

public class Setting extends BaseActivity implements Switch.OnCheckedChangeListener
        , View.OnClickListener {
    private Toolbar toolbar;
    private RadioGroup group;
    private String[] time, enableDownload;
    private TextView filter;
    private TextView cache;
    private MusicUtils musicUtils;
    private Dialog dialog;
    private TextView downloadText;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);

        time = new String[]{"0s", "15s", "20s", "30s", "40s", "50s", "60s"};
        enableDownload = new String[]{getResources().getString(R.string.never),
                getResources().getString(R.string.always),
                getResources().getString(R.string.only_wifi)};

        musicUtils = new MusicUtils(this);

        findViews();

        toolbar.setTitle(getResources().getString(R.string.settings));
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.listView_bg_color));

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                switch (checkedId) {
                    case R.id.page_music:
                        MusicUtils.launchPage = 1;
                        break;
                    case R.id.page_artist:
                        MusicUtils.launchPage = 2;
                        break;
                    case R.id.page_album:
                        MusicUtils.launchPage = 3;
                        break;
                    case R.id.page_playlist:
                        MusicUtils.launchPage = 4;
                        break;
                    case R.id.page_recent:
                        MusicUtils.launchPage = 5;
                        break;
                    case R.id.page_folder:
                        MusicUtils.launchPage = 6;
                        break;
                }
                editor.putInt("launchPage", MusicUtils.launchPage);
                editor.apply();
                editor.commit();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void findViews() {

        RelativeLayout clearCache = (RelativeLayout) findViewById(R.id.layout_cache);
        filter = (TextView) findViewById(R.id.filter_text);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        Switch defaultAlbumArt = (Switch) findViewById(R.id.use_default_cover);
        Switch colorNotification = (Switch) findViewById(R.id.color_notification);
        Switch lockscreenNotification = (Switch) findViewById(R.id.lockscreen_album_art);
        Switch keepScreenOn = (Switch) findViewById(R.id.keep_screen_on);
        Switch loadWebLyric = (Switch) findViewById(R.id.load_web_lyric);
        Switch enableShuffle = (Switch) findViewById(R.id.enable_shuffle);
        cache = (TextView) findViewById(R.id.clear_cache);
        TextView equalizer = (TextView) findViewById(R.id.equalizer);
        TextView about = (TextView) findViewById(R.id.about);
        RelativeLayout download = (RelativeLayout) findViewById(R.id.download_layout);
        downloadText = (TextView) findViewById(R.id.download_artist);

        cache.setText(musicUtils.getCacheSize(this));
        group = (RadioGroup) findViewById(R.id.page_group);

        RadioButton musicButton = (RadioButton) findViewById(R.id.page_music);
        RadioButton albumButton = (RadioButton) findViewById(R.id.page_album);
        RadioButton artistButton = (RadioButton) findViewById(R.id.page_artist);
        RadioButton recentButton = (RadioButton) findViewById(R.id.page_recent);
        RadioButton playlistButton = (RadioButton) findViewById(R.id.page_playlist);
        RadioButton folderButton = (RadioButton) findViewById(R.id.page_folder);


        filter.setTextColor(Color.BLACK);
        switch (MusicUtils.filterNum) {
            case 0:
                filter.setText(getResources().getString(R.string.ignore) + " 0 " + getResources().getString(R.string.second));
                break;
            case 1:
                filter.setText(getResources().getString(R.string.ignore) + " 15 " + getResources().getString(R.string.second));
                break;
            case 2:
                filter.setText(getResources().getString(R.string.ignore) + " 20 " + getResources().getString(R.string.second));
                break;
            case 3:
                filter.setText(getResources().getString(R.string.ignore) + " 30 " + getResources().getString(R.string.second));
                break;
            case 4:
                filter.setText(getResources().getString(R.string.ignore) + " 40 " + getResources().getString(R.string.second));
                break;
            case 5:
                filter.setText(getResources().getString(R.string.ignore) + " 50 " + getResources().getString(R.string.second));
                break;
            case 6:
                filter.setText(getResources().getString(R.string.ignore) + " 60 " + getResources().getString(R.string.second));
                break;
        }

        switch (MusicUtils.downloadArtist){
            case 0:
                downloadText.setText(getResources().getString(R.string.never));
                break;
            case 1:
                downloadText.setText(getResources().getString(R.string.always));
                break;
            case 2:
                downloadText.setText(getResources().getString(R.string.only_wifi));
                break;

        }
        switch (MusicUtils.launchPage) {
            case 1:
                musicButton.setChecked(true);
                break;
            case 2:
                artistButton.setChecked(true);
                break;
            case 3:
                albumButton.setChecked(true);
                break;
            case 4:
                playlistButton.setChecked(true);
                break;
            case 5:
                recentButton.setChecked(true);
                break;
            case 6:
                folderButton.setChecked(true);
                break;

        }

        defaultAlbumArt.setChecked(MusicUtils.enableDefaultCover);
        colorNotification.setChecked(MusicUtils.enableColorNotification);
        lockscreenNotification.setChecked(MusicUtils.enableLockscreenNotification);
        keepScreenOn.setChecked(MusicUtils.keepScreenOn);
        loadWebLyric.setChecked(MusicUtils.loadWebLyric);
        enableShuffle.setChecked(MusicUtils.enableShuffle);

        defaultAlbumArt.setOnCheckedChangeListener(this);
        colorNotification.setOnCheckedChangeListener(this);
        lockscreenNotification.setOnCheckedChangeListener(this);
        keepScreenOn.setOnCheckedChangeListener(this);
        loadWebLyric.setOnCheckedChangeListener(this);
        enableShuffle.setOnCheckedChangeListener(this);

        equalizer.setOnClickListener(this);
        filter.setOnClickListener(this);
        about.setOnClickListener(this);
        clearCache.setOnClickListener(this);
        download.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        switch (buttonView.getId()) {
            case R.id.use_default_cover:
                MusicUtils.enableDefaultCover = isChecked;
                editor.putBoolean("enableDefaultCover", MusicUtils.enableDefaultCover);
                editor.apply();
                break;
            case R.id.color_notification:
                MusicUtils.enableColorNotification = isChecked;
                editor.putBoolean("enableColorNotification", MusicUtils.enableColorNotification);
                editor.apply();
                break;
            case R.id.lockscreen_album_art:
                MusicUtils.enableLockscreenNotification = isChecked;
                editor.putBoolean("enableLockscreenNotification", MusicUtils.enableLockscreenNotification);
                editor.apply();
                break;
            case R.id.keep_screen_on:
                MusicUtils.keepScreenOn = isChecked;
                editor.putBoolean("keepScreenOn", MusicUtils.keepScreenOn);
                editor.apply();
                break;
            case R.id.load_web_lyric:
                MusicUtils.loadWebLyric = isChecked;
                editor.putBoolean("loadWebLyric", MusicUtils.loadWebLyric);
                editor.apply();
                break;
            case R.id.enable_shuffle:
                MusicUtils.enableShuffle = isChecked;
                editor.putBoolean("enableShuffle", MusicUtils.enableShuffle);
                editor.apply();
                Intent intent = new Intent("enableShuffle");
                sendBroadcast(intent);
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

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        musicUtils.clearImageAllCache(getApplicationContext());
                        if (musicUtils.getCacheSize(getApplicationContext()).equals("0.0Byte")) {
                            Message message = new Message();
                            message.what = 13;
                            handler.sendMessage(message);
                        }
                    }
                }).start();
            }
            if (msg.what == 13 && dialog.isShowing()) {
                dialog.dismiss();
            }
            cache.setText(musicUtils.getCacheSize(getApplicationContext()));
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
                clearAlert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                clearAlert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Message message = new Message();
                        message.what = 11;
                        handler.sendMessage(message);
                    }
                });
                clearAlert.show();
                break;
            case R.id.filter_text:
                AlertDialog.Builder filterDialog = new AlertDialog.Builder(this);
                filterDialog.setTitle("Ignore");

                filterDialog.setSingleChoiceItems(time, MusicUtils.filterNum, null);
                filterDialog.setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                filterDialog.setPositiveButton(R.string.delete_confrim, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MusicUtils.filterNum = ((AlertDialog) dialog).getListView().getCheckedItemPosition();

                        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        editor.putInt("filterNum", MusicUtils.filterNum);
                        editor.apply();
                        switch (MusicUtils.filterNum) {
                            case 0:
                                filter.setText(getResources().getString(R.string.ignore) + " 0 " + getResources().getString(R.string.second));
                                break;
                            case 1:
                                filter.setText(getResources().getString(R.string.ignore) + " 15 " + getResources().getString(R.string.second));
                                break;
                            case 2:
                                filter.setText(getResources().getString(R.string.ignore) + " 20 " + getResources().getString(R.string.second));
                                break;
                            case 3:
                                filter.setText(getResources().getString(R.string.ignore) + " 30 " + getResources().getString(R.string.second));
                                break;
                            case 4:
                                filter.setText(getResources().getString(R.string.ignore) + " 40 " + getResources().getString(R.string.second));
                                break;
                            case 5:
                                filter.setText(getResources().getString(R.string.ignore) + " 50 " + getResources().getString(R.string.second));
                                break;
                            case 6:
                                filter.setText(getResources().getString(R.string.ignore) + " 60 " + getResources().getString(R.string.second));
                                break;
                        }
                    }
                });

                filterDialog.show();
                break;
            case R.id.equalizer:
                Intent in = new Intent(AudioEffect
                        .ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);

                if ((in.resolveActivity(getPackageManager()) != null)) {
                    startActivityForResult(in, 0);
                } else {
                    Intent i = new Intent(this, Equalizer.class);
                    startActivity(i);

                }
                break;
            case R.id.download_layout:
                AlertDialog.Builder downloadDialog = new AlertDialog.Builder(this);

                downloadDialog.setSingleChoiceItems(enableDownload, MusicUtils.downloadArtist, null);
                downloadDialog.setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                downloadDialog.setPositiveButton(R.string.delete_confrim, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MusicUtils.downloadArtist = ((AlertDialog) dialog).getListView().getCheckedItemPosition();

                        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        editor.putInt("downloadArtist", MusicUtils.downloadArtist);
                        editor.apply();
                        switch (MusicUtils.downloadArtist) {
                            case 0:
                                downloadText.setText(getResources().getString(R.string.never));
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Setting.this);
                                alertDialog.setTitle(R.string.delete_download_image);
                                alertDialog.setCancelable(true);
                                alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                                alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        File file = new File(MusicUtils.localPath, MusicUtils.folder);
                                        MusicUtils.deleteDownloadImage(file);

                                    }
                                });
                                alertDialog.show();
                                break;
                            case 1:
                                downloadText.setText(getResources().getString(R.string.always));
                                break;
                            case 2:
                                downloadText.setText(getResources().getString(R.string.only_wifi));
                                break;

                        }
                    }
                });

                downloadDialog.show();
                break;
        }
    }
}

package ironbear775.com.musicplayer.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;

import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2017/3/28.
 */

public class Setting extends BaseActivity implements Switch.OnCheckedChangeListener, View.OnClickListener {
    private Toolbar toolbar;
    private RadioGroup group;
    private String[] time;
    private TextView filter;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);

        time = new String[]{"0s","15s", "20s", "30s", "40s", "50s", "60s"};

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

        filter = (TextView) findViewById(R.id.filter_text);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        Switch defaultAlbumArt = (Switch) findViewById(R.id.use_default_cover);
        Switch colorNotification = (Switch) findViewById(R.id.color_notification);
        Switch lockscreenNotification = (Switch) findViewById(R.id.lockscreen_album_art);
        Switch downloadCover = (Switch) findViewById(R.id.download_cover);
        Switch keepScreenOn = (Switch) findViewById(R.id.keep_screen_on);
        Switch loadWebLyric = (Switch) findViewById(R.id.load_web_lyric);

        group = (RadioGroup) findViewById(R.id.page_group);

        RadioButton musicButton = (RadioButton) findViewById(R.id.page_music);
        RadioButton albumButton = (RadioButton) findViewById(R.id.page_album);
        RadioButton artistButton = (RadioButton) findViewById(R.id.page_artist);
        RadioButton recentButton = (RadioButton) findViewById(R.id.page_recent);
        RadioButton playlistButton = (RadioButton) findViewById(R.id.page_playlist);

        TextView about = (TextView) findViewById(R.id.about);

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

        }

        defaultAlbumArt.setChecked(MusicUtils.enableDefaultCover);
        colorNotification.setChecked(MusicUtils.enableColorNotification);
        lockscreenNotification.setChecked(MusicUtils.enableLockscreenNotification);
        downloadCover.setChecked(MusicUtils.enableDownload);
        keepScreenOn.setChecked(MusicUtils.keepScreenOn);
        loadWebLyric.setChecked(MusicUtils.loadWebLyric);

        defaultAlbumArt.setOnCheckedChangeListener(this);
        colorNotification.setOnCheckedChangeListener(this);
        lockscreenNotification.setOnCheckedChangeListener(this);
        downloadCover.setOnCheckedChangeListener(this);
        keepScreenOn.setOnCheckedChangeListener(this);
        loadWebLyric.setOnCheckedChangeListener(this);

        filter.setOnClickListener(this);
        about.setOnClickListener(this);
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
            case R.id.download_cover:
                MusicUtils.enableDownload = isChecked;
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
                if (!isChecked) {
                    alertDialog.show();
                }
                editor.putBoolean("enableDownload", MusicUtils.enableDownload);

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
        }
        editor.commit();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.about) {
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.filter_text) {
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
                    MusicUtils.filterNum = ((AlertDialog)dialog).getListView().getCheckedItemPosition();

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

        }
    }
}

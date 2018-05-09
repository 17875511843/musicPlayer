package ironbear775.com.musicplayer.Activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.MusicService;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2017/6/24.
 */

public class Equalizer extends BaseActivity implements SeekBar.OnSeekBarChangeListener, Switch.OnCheckedChangeListener {
    private Toolbar toolbar;
    private Button reset;
    private Switch equalizerSwitch;
    private android.media.audiofx.Equalizer equalizer;
    public static int Max = 6;
    private SeekBar seekBar[] = new SeekBar[Max];
    private TextView minLable[] = new TextView[Max];
    private TextView maxLable[] = new TextView[Max];
    private RelativeLayout layout[] = new RelativeLayout[Max];
    private int num = 0;
    private int min_level = 0;
    private int max_level = 100;
    private int save[] = new int[Max];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.equalizer_layout);
        findView();
        toolbar.setTitle(R.string.equalizer);
        toolbar.setTitleTextColor(Color.WHITE);
        reset.setTextColor(Color.WHITE);
        equalizerSwitch.setChecked(MusicUtils.getInstance().enableEqualizer);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        equalizer = new android.media.audiofx.Equalizer(0, MusicService.mediaPlayer.getAudioSessionId());

        setEqualizer();
    }

    public void setEqualizer() {
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);

        num = equalizer.getNumberOfBands();
        short r[] = equalizer.getBandLevelRange();
        min_level = r[0];
        max_level = r[1];
        for (int i = 0; i < num && i < Max; i++) {
            int[] freq_range = equalizer.getBandFreqRange((short) i);
            int level = sharedPreferences.getInt("equalizer" + i,
                    equalizer.getBandLevel((short) i));

            equalizer.setBandLevel((short) i, (short) level);
            seekBar[i].setOnSeekBarChangeListener(this);
            minLable[i].setText(milliHzToString(freq_range[0]));
            maxLable[i].setText(milliHzToString(freq_range[1]));

        }
        for (int i = num; i < Max; i++) {
            seekBar[i].setVisibility(View.GONE);
            minLable[i].setVisibility(View.GONE);
            maxLable[i].setVisibility(View.GONE);
            layout[i].setVisibility(View.GONE);
        }
        if (equalizerSwitch.isChecked()) {
            equalizer.setEnabled(true);
            for (int i = 0; i < num; i++) {
                seekBar[i].setEnabled(true);
            }
        } else {
            equalizer.setEnabled(false);
            for (int i = 0; i < num; i++) {
                seekBar[i].setEnabled(false);
            }
        }
        updateUI();
    }

    public void updateUI() {
        updateSeekBars();
    }

    public void updateSeekBars() {
        for (int i = 0; i < num; i++) {
            int level;
            if (equalizer != null)
                level = equalizer.getBandLevel((short) i);
            else
                level = 0;
            int pos = 100 * level / (max_level - min_level) + 50;
            seekBar[i].setProgress(pos);
        }
    }

    public String milliHzToString(int milliHz) {
        if (milliHz < 1000) return "";
        if (milliHz < 1000000)
            return "" + (milliHz / 1000) + "Hz";
        else
            return "" + (milliHz / 1000000) + "kHz";
    }

    private void findView() {
        toolbar = findViewById(R.id.equalizer_toolbar);
        reset = findViewById(R.id.reset_equalizer);
        equalizerSwitch = findViewById(R.id.equalizer_switch);
        equalizerSwitch.setOnCheckedChangeListener(this);
        reset.setOnClickListener(v -> {
            if (!equalizerSwitch.isChecked()) {
                equalizerSwitch.setChecked(true);
            }

            for (int i = 0; i < num; i++) {
                seekBar[i].setProgress(50);
            }
        });
        seekBar[0] = findViewById(R.id.equalizer1);
        minLable[0] = findViewById(R.id.min1);
        maxLable[0] = findViewById(R.id.max1);
        layout[0] = findViewById(R.id.layout1);

        seekBar[1] = findViewById(R.id.equalizer2);
        minLable[1] = findViewById(R.id.min2);
        maxLable[1] = findViewById(R.id.max2);
        layout[1] = findViewById(R.id.layout2);

        seekBar[2] = findViewById(R.id.equalizer3);
        minLable[2] = findViewById(R.id.min3);
        maxLable[2] = findViewById(R.id.max3);
        layout[2] = findViewById(R.id.layout3);

        seekBar[3] = findViewById(R.id.equalizer4);
        minLable[3] = findViewById(R.id.min4);
        maxLable[3] = findViewById(R.id.max4);
        layout[3] = findViewById(R.id.layout4);

        seekBar[4] = findViewById(R.id.equalizer5);
        minLable[4] = findViewById(R.id.min5);
        maxLable[4] = findViewById(R.id.max5);
        layout[4] = findViewById(R.id.layout5);

        seekBar[5] = findViewById(R.id.equalizer6);
        minLable[5] = findViewById(R.id.min6);
        maxLable[5] = findViewById(R.id.max6);
        layout[5] = findViewById(R.id.layout6);

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        if (buttonView.getId() == R.id.equalizer_switch) {
            MusicUtils.getInstance().enableEqualizer = isChecked;
            editor.putBoolean("enableEqualizer", MusicUtils.getInstance().enableEqualizer);
            editor.apply();
            editor.commit();
        }
        if (isChecked) {
            equalizer = new android.media.audiofx.Equalizer(0, MusicService.mediaPlayer.getAudioSessionId());

            equalizer.setEnabled(true);
            for (int i = 0; i < num; i++) {
                seekBar[i].setEnabled(true);
            }
        } else {
            equalizer.setEnabled(false);
            for (int i = 0; i < num; i++) {
                seekBar[i].setEnabled(false);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seek, int progress, boolean fromUser) {
        if (equalizer != null && equalizerSwitch.isChecked()) {
            SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();

            int new_level = min_level + (max_level - min_level) * progress / 100;

            for (int i = 0; i < num; i++) {
                if (seek == seekBar[i]) {
                    equalizer.setBandLevel((short) i, (short) new_level);
                    break;
                }
            }
            for (int i = 0; i < num; i++) {
                save[i] = equalizer.getBandLevel((short) i);
                editor.putInt("equalizer" + i, save[i]);
                editor.apply();
            }
            editor.commit();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        equalizer.release();
    }
}

package ironbear775.com.musicplayer.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Service.SleepService;
import ironbear775.com.musicplayer.Util.TimePickerView;

/**
 * Created by ironbear on 2016/12/19.
 */

public class SleepTimer extends Activity {
    private int timer;
    private int mMinute = 30;
    private int mSecond = 30;
    private TextView leftTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sleep_timer_new_layout);
        Button confrim = (Button) findViewById(R.id.sleepStart);
        final Button cancel = (Button) findViewById(R.id.sleepCancel);
        leftTime = (TextView) findViewById(R.id.sleep_time);
        TimePickerView minutePicker = (TimePickerView) findViewById(R.id.minute_pv);
        TimePickerView secondPicker = (TimePickerView) findViewById(R.id.second_pv);
        List<String> data = new ArrayList<>();
        List<String> seconds = new ArrayList<>();
        for (int i = 0; i < 60; i++)
        {
            data.add(i < 10 ? "0" + i : "" + i);
        }
        for (int i = 0; i < 60; i++)
        {
            seconds.add(i < 10 ? "0" + i : "" + i);
        }
        minutePicker.setData(data);
        minutePicker.setOnSelectListener(new TimePickerView.onSelectListener()
        {

            @Override
            public void onSelect(String text)
            {
                mMinute = Integer.valueOf(text);
            }
        });
        secondPicker.setData(seconds);
        secondPicker.setOnSelectListener(new TimePickerView.onSelectListener()
        {

            @Override
            public void onSelect(String text)
            {
                mSecond = Integer.valueOf(text);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SleepTimer.this, SleepService.class);
                stopService(intent);
                leftTime.setVisibility(View.GONE);
                Snackbar.make(MusicList.PlayOrPause,getResources().getString(R.string.delete_cancel),Snackbar.LENGTH_SHORT)
                        .setDuration(1000)
                        .show();
                finish();
            }
        });
        confrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                timer = mMinute*60+mSecond;
                if (timer > 0) {
                    Intent intent1 = new Intent(SleepTimer.this, SleepService.class);
                    stopService(intent1);
                    Intent intent = new Intent(SleepTimer.this, SleepService.class);
                    intent.putExtra("time", timer);
                    startService(intent);
                    finish();
                    Snackbar.make(MusicList.PlayOrPause,getResources().getString(R.string.set_success),Snackbar.LENGTH_SHORT)
                            .setDuration(1000)
                            .show();
                } else {
                    Snackbar.make(cancel,getResources().getString(R.string.wrong_time),Snackbar.LENGTH_SHORT)
                            .setDuration(1000)
                            .show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        registerReceiver(mUpdateReceiver, updateIntentFilter());
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mUpdateReceiver);
        super.onPause();
    }

    private final BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case "CountDown Running":
                    // 正在倒计时
                    String min;
                    String sec;
                    int total = intent.getIntExtra("time", 0);
                    int minute = total / 60;
                    int second = total - minute * 60;
                    if (minute < 10) {
                        min = "0" + minute;
                    } else {
                        min = "" + minute;
                    }
                    if (second < 10) {
                        sec = "0" + second;
                    } else {
                        sec = "" + second;
                    }
                    leftTime.setText(min + ":" + sec);
                    break;
            }
        }
    };

    //注册广播
    private static IntentFilter updateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SleepService.IS_RUNNING);
        return intentFilter;
    }
}

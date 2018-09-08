package ironbear775.com.musicplayer.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sdsmdg.harjot.crollerTest.Croller;
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener;

import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.service.SleepService;
import ironbear775.com.musicplayer.util.MusicUtils;

/**
 * Created by ironbear on 2016/12/19.
 */

public class SleepTimer extends Dialog {
    private int mMinute;
    private TextView leftTime;
    private Context mContext;

    SleepTimer(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mContext.registerReceiver(mUpdateReceiver, updateIntentFilter());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMinute = MusicUtils.getInstance().sleepTime;
        setContentView(R.layout.sleep_timer_new_layout);
        Button confirm =  findViewById(R.id.sleepStart);
        final Button cancel =  findViewById(R.id.sleepCancel);
        leftTime =  findViewById(R.id.sleep_time);
        Croller croller = findViewById(R.id.picker);

        croller.setProgress(mMinute);
        String string = mMinute+mContext.getResources().getString(R.string.minute);
        croller.setLabel(string);

        croller.setOnCrollerChangeListener(new OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(Croller croller, int progress) {
                String string = progress+mContext.getResources().getString(R.string.minute);
                croller.setLabel(string);
            }

            @Override
            public void onStartTrackingTouch(Croller croller) {

            }

            @Override
            public void onStopTrackingTouch(Croller croller) {
                mMinute = croller.getProgress();
                String string = mMinute+mContext.getResources().getString(R.string.minute);
                croller.setLabel(string);
            }
        });

        cancel.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, SleepService.class);
            mContext.stopService(intent);
            leftTime.setVisibility(View.GONE);

            Intent intent1 = new Intent("show snackBar");
            intent1.putExtra("text id",R.string.delete_cancel);
            mContext.sendBroadcast(intent1);
        });

        confirm.setOnClickListener(v -> {

            if (mMinute > 0) {
                Intent intent1 = new Intent(mContext, SleepService.class);
                mContext.stopService(intent1);
                Intent intent = new Intent(mContext, SleepService.class);
                intent.putExtra("time", mMinute);
                mContext.startService(intent);

                Intent intent2 = new Intent("show snackBar");
                intent2.putExtra("text id",R.string.set_success);
                mContext.sendBroadcast(intent2);
                MusicUtils.getInstance().sleepTime = mMinute;
                SharedPreferences sharedPreferences = mContext.getSharedPreferences("data",Context.MODE_PRIVATE);
                sharedPreferences.edit().putInt("sleepTime",mMinute).apply();
                hide();
            } else {
                Snackbar.make(cancel,R.string.wrong_time,Snackbar.LENGTH_SHORT)
                        .setDuration(1000)
                        .show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mContext.unregisterReceiver(mUpdateReceiver);
    }

    private final BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
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
                        String t = min + ":" + sec;
                        leftTime.setText(t);
                        break;
                }
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

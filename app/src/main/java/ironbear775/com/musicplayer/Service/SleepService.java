package ironbear775.com.musicplayer.Service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by ironbear on 2016/12/19.
 */

public class SleepService extends Service {
    private CountDownTimer countDownTimer;
    public static final String IS_RUNNING = "CountDown Running";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int totalTime = intent.getIntExtra("time", 0) * 1000 * 60;
        countDownTimer = new CountDownTimer(totalTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                broadcastUpdate(IS_RUNNING, (int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                if (MusicService.mediaPlayer!=null) {
                    sendBroadcast(new Intent("clear"));
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();

                    editor.putInt("progress", MusicService.mediaPlayer.getCurrentPosition());
                    editor.putInt("flag", 0);
                    editor.apply();
                    editor.commit();
                }
                stopSelf();
            }
        };
        countDownTimer.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 发送带有数据的广播
    private void broadcastUpdate(final String action, int time) {
        final Intent intent = new Intent(action);
        intent.putExtra("time", time);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        countDownTimer.cancel();
        super.onDestroy();
    }
}


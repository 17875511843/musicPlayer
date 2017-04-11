package ironbear775.com.musicplayer.Service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import ironbear775.com.musicplayer.Activity.ActivityCollector;
import ironbear775.com.musicplayer.Fragment.AlbumDetailFragment;
import ironbear775.com.musicplayer.Fragment.AlbumListFragment;
import ironbear775.com.musicplayer.Fragment.ArtistDetailFragment;
import ironbear775.com.musicplayer.Fragment.MusicListFragment;
import ironbear775.com.musicplayer.Fragment.MusicRecentAddedFragment;
import ironbear775.com.musicplayer.Fragment.PlaylistDetailFragment;

/**
 * Created by ironbear on 2016/12/19.
 */

public class SleepService extends Service {
    private CountDownTimer countDownTimer;
    public static final String IS_RUNNING = "CountDown Running";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int totalTime = intent.getIntExtra("time", 0) * 1000;
        countDownTimer = new CountDownTimer(totalTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                broadcastUpdate(IS_RUNNING, (int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(1);
                SharedPreferences.Editor editor;
                editor = getSharedPreferences("data", MODE_PRIVATE).edit();

                if (MusicService.mediaPlayer.isPlaying()) {
                    for (int i = 0; i < MusicListFragment.musicList.size(); i++) {
                        if (MusicListFragment.musicList.get(i).getUri()
                                .equals(MusicService.musicList
                                        .get(MusicService.musicPosition).getUri())) {
                            editor.putInt("position", i);
                            break;
                        }
                    }
                } else if (MusicListFragment.count == 1) {
                    editor.putInt("position", MusicListFragment.pos);
                } else if (ArtistDetailFragment.count == 1) {
                    for (int i = 0; i < MusicListFragment.musicList.size(); i++) {
                        if (MusicListFragment.musicList.get(i).getUri()
                                .equals(ArtistDetailFragment.musicList
                                        .get(ArtistDetailFragment.pos).getUri())) {
                            editor.putInt("position", i);
                            break;
                        }
                    }
                } else if (AlbumDetailFragment.count == 1) {
                    for (int i = 0; i < MusicListFragment.musicList.size(); i++) {
                        if (MusicListFragment.musicList.get(i).getUri()
                                .equals(AlbumDetailFragment.musicList
                                        .get(AlbumListFragment.pos).getUri())) {
                            editor.putInt("position", i);
                            break;
                        }
                    }
                } else if (PlaylistDetailFragment.count == 1) {
                    for (int i = 0; i < MusicListFragment.musicList.size(); i++) {
                        if (MusicListFragment.musicList.get(i).getUri()
                                .equals(PlaylistDetailFragment.musicList
                                        .get(PlaylistDetailFragment.pos).getUri())) {
                            editor.putInt("position", i);
                            break;
                        }
                    }
                } else if (MusicRecentAddedFragment.count == 1) {
                    for (int i = 0; i < MusicListFragment.musicList.size(); i++) {
                        if (MusicListFragment.musicList.get(i).getUri()
                                .equals(MusicRecentAddedFragment.musicList
                                        .get(MusicRecentAddedFragment.pos).getUri())) {
                            editor.putInt("position", i);
                            break;
                        }
                    }
                } else {
                    editor.putInt("position", MusicListFragment.pos);
                }

                MusicService.mediaPlayer.pause();
                int progress = MusicService.mediaPlayer.getCurrentPosition();
                Intent intent1 = new Intent(SleepService.this, MusicService.class);
                stopService(intent1);

                editor.putInt("progress", progress);
                editor.putInt("flag", 0);
                editor.apply();
                editor.commit();
                stopSelf();
                ActivityCollector.finishAll();
                System.exit(0);
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


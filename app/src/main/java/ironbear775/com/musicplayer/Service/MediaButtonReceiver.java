package ironbear775.com.musicplayer.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import ironbear775.com.musicplayer.Activity.BaseActivity;

/**
 * Created by ironbear on 2017/4/29.
 */

public class MediaButtonReceiver extends BroadcastReceiver {
    private static String TAG = "MediaButtonReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        // 获得Action
        String intentAction = intent.getAction();
        // 获得KeyEvent对象
        KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            int keyCode = keyEvent.getKeyCode();
            int keyAction = keyEvent.getAction();

            if (KeyEvent.KEYCODE_MEDIA_NEXT == keyCode && keyAction == KeyEvent.ACTION_UP) {
                Intent in = new Intent("NEXT");
                BaseActivity.myContext.sendBroadcast(in);Log.d(TAG, "5");
            }
            if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == keyCode && keyAction == KeyEvent.ACTION_UP) {
                Intent in = new Intent("PlAYORPAUSE");
                BaseActivity.myContext.sendBroadcast(in);
                Log.d(TAG, "1");
            }
            if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode  && keyAction == KeyEvent.ACTION_UP) {
                Intent in = new Intent("PlAYORPAUSE");
                BaseActivity.myContext.sendBroadcast(in);Log.d(TAG, "2");
            }
            if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == keyCode && keyAction == KeyEvent.ACTION_UP) {
                Intent in = new Intent("PREVIOUS");
                BaseActivity.myContext.sendBroadcast(in);Log.d(TAG, "3");
            }
            if (KeyEvent.KEYCODE_MEDIA_STOP == keyCode && keyAction == KeyEvent.ACTION_UP) {
                Intent in = new Intent("STOP");
                BaseActivity.myContext.sendBroadcast(in);Log.d(TAG, "4");
            }
        }
    }
}
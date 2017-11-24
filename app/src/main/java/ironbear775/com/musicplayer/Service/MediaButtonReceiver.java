package ironbear775.com.musicplayer.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by ironbear on 2017/4/29.
 */

public class MediaButtonReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 获得Action
        String intentAction = intent.getAction();
        // 获得KeyEvent对象
        KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            int keyCode = keyEvent.getKeyCode();
            int keyAction = keyEvent.getAction();

            String TAG = "MediaButtonReceiver";
            if (KeyEvent.KEYCODE_MEDIA_NEXT == keyCode && keyAction == KeyEvent.ACTION_UP) {
                Intent in = new Intent("NEXT");
                context.sendBroadcast(in);Log.d(TAG, "5");
            }
            if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == keyCode && keyAction == KeyEvent.ACTION_UP) {
                Intent in = new Intent("PlAYORPAUSE");
                context.sendBroadcast(in);
            }
            if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode  && keyAction == KeyEvent.ACTION_UP) {
                Intent in = new Intent("PlAYORPAUSE");
                context.sendBroadcast(in);Log.d(TAG, "2");
            }
            if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == keyCode && keyAction == KeyEvent.ACTION_UP) {
                Intent in = new Intent("PREVIOUS");
                context.sendBroadcast(in);Log.d(TAG, "3");
            }
            if (KeyEvent.KEYCODE_MEDIA_STOP == keyCode && keyAction == KeyEvent.ACTION_UP) {
                Intent in = new Intent("STOP");
                context.sendBroadcast(in);Log.d(TAG, "4");
            }
        }
    }
}
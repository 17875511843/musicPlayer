package ironbear775.com.musicplayer.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import java.util.Objects;


/**
 * Created by ironbear on 2017/5/12.
 */

public class OpenActivity extends BaseActivity {
    private OpenFileReceiver receiver;
    private String path;
    private static final String TAG = "OpenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            path = Objects.requireNonNull(getIntent().getData()).getPath();

            receiver = new OpenFileReceiver();
            registerReceiver(receiver, new IntentFilter("open activity"));

            if (!ActivityCollector.isActivityExist(MusicList.class)) {

                Intent intent = new Intent(this, MusicList.class);
                intent.putExtra("Start Activity", true);
                startActivity(intent);

            } else {
                send();
                startActivity(new Intent(this, MusicList.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void send() {
        Intent oIntent = new Intent("open file");
        oIntent.putExtra("path", path);
        Log.e(TAG, "send: "+path);
        sendBroadcast(oIntent);
        finish();
    }

    private class OpenFileReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), "open activity")) {
                send();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}

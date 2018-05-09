package ironbear775.com.musicplayer.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.bumptech.glide.Glide;

/**
 * Created by ironbear on 2017/11/6.
 */

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences SharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());


        boolean isFirstStart = SharedPreferences.getBoolean("firstStart", true);

        try {
            int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0)
                    .versionCode;
            int oldVersionCode = SharedPreferences.getInt("versionCode", 0);

            SharedPreferences.Editor editor = SharedPreferences.edit();
            editor.putInt("versionCode", versionCode);
            editor.apply();

            if (isFirstStart) {
                editor.putBoolean("firstStart", false);
                editor.apply();

                Intent i = new Intent(MainActivity.this, MyIntroActivity.class);
                startActivity(i);
            } else {
                Intent intent = new Intent(MainActivity.this, MusicList.class);
                if (oldVersionCode < versionCode)
                    intent.putExtra("IS_NEW_VERSION", true);
                else
                    intent.putExtra("IS_NEW_VERSION", false);

                startActivity(intent);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}

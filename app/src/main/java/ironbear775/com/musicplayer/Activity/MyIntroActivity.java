package ironbear775.com.musicplayer.Activity;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;

import ironbear775.com.musicplayer.Fragment.IntroDemoFragment;
import ironbear775.com.musicplayer.Fragment.IntroFragment;
import ironbear775.com.musicplayer.R;

/**
 * Created by ironbear on 2017/1/29.
 */

public class MyIntroActivity extends AppIntro2 {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment fragment = new IntroFragment();
        Fragment demoFragment = new IntroDemoFragment();

        addSlide(AppIntro2Fragment.newInstance(getResources().getString(R.string.welcome),
                getResources().getString(R.string.intro),
                R.drawable.album, Color.parseColor("#2196f3")));
        addSlide(AppIntro2Fragment.newInstance(getResources().getString(R.string.feature),
                getResources().getString(R.string.feature_intro),
                R.drawable.feature, Color.parseColor("#00BCD4")));
        addSlide(demoFragment);
        addSlide(fragment);
        addSlide(AppIntro2Fragment.newInstance(getResources().getString(R.string.enjoy),
                getResources().getString(R.string.alldone),
                R.drawable.done, Color.parseColor("#00BCD4")));

        setDepthAnimation();
        showSkipButton(false);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(getBaseContext(), MusicList.class);
            intent.putExtra("IS_NEW_VERSION", true);
            startActivity(intent);
        }
    }
}

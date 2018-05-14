package ironbear775.com.musicplayer.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.umeng.analytics.MobclickAgent;

import java.util.Calendar;

import ironbear775.com.musicplayer.MyApplication;
import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2016/11/7.
 */

public class BaseActivity extends AppCompatActivity {
    public static boolean isNight = false;
    public static boolean changeMainWindow = true;
    private MyApplication application;
    private BaseActivity oContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this, getClass());
        SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        isNight = sp.getBoolean("isNight", false);
        changeMainWindow = sp.getBoolean("changeMainWindow", true);
        MusicUtils.getInstance().themeName = sp.getInt("themeName", R.string.color_Sandalwood);
        MusicUtils.getInstance().autoSwitchNightMode = sp.getBoolean("autoSwitchNightMode", true);
        boolean isManual = sp.getBoolean("isManual", false);

        long time = System.currentTimeMillis();

        final Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(time);
        int mHour = mCalendar.get(Calendar.HOUR_OF_DAY);

        if (MusicUtils.getInstance().autoSwitchNightMode && !isManual) {
            if ((mHour >= 22 || (0 <= mHour && mHour < 7)))
                isNight = true;
        } else if (isManual) {
            if (isNight)
                setTheme(R.style.MaterialDrawerThemeNight);
            sp.edit().putBoolean("isManual", false).apply();
        }

        if (isNight)
            setTheme(R.style.MaterialDrawerThemeNight);
        else if (changeMainWindow){
            if (R.string.color_Blue == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeBlue);
            else if (R.string.color_Green == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeGreen);
            else if (R.string.color_Indigo == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeIndigo);
            else if (R.string.color_Pink == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemePink);
            else if (R.string.color_Purple == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemePurple);
            else if (R.string.color_Red == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeRed);
            else if (R.string.color_LightBlue == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeLightBlue);
            else if (R.string.color_Teal == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeTeal);
            else if (R.string.color_Lime == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeLime);
            else if (R.string.color_Orange == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeOrange);
            else if (R.string.color_DeepOrange == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeDeepOrange);
            else if (R.string.color_Brown == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeBrown);
            else if (R.string.color_Grey == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeGrey);
            else if (R.string.color_Carmine == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeCarmine);
            else if (R.string.color_Amber == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeAmber);
            else if (R.string.color_DarkBlue1 == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeDarkBlue);
            else if (R.string.color_Sandalwood == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeSandalwood);
            else if (R.string.color_BambooGreen == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeBambooGreen);
            else if (R.string.color_BlueGrey == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeBlueGrey);
        }else {
            if (R.string.color_Blue == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeBlueNoChangeMainWindowColor);
            else if (R.string.color_Green == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeGreenNoChangeMainWindowColor);
            else if (R.string.color_Indigo == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeIndigoNoChangeMainWindowColor);
            else if (R.string.color_Pink == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemePinkNoChangeMainWindowColor);
            else if (R.string.color_Purple == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemePurpleNoChangeMainWindowColor);
            else if (R.string.color_Red == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeRedNoChangeMainWindowColor);
            else if (R.string.color_LightBlue == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeLightBlueNoChangeMainWindowColor);
            else if (R.string.color_Teal == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeTealNoChangeMainWindowColor);
            else if (R.string.color_Lime == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeLimeNoChangeMainWindowColor);
            else if (R.string.color_Orange == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeOrangeNoChangeMainWindowColor);
            else if (R.string.color_DeepOrange == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeDeepOrangeNoChangeMainWindowColor);
            else if (R.string.color_Brown == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeBrownNoChangeMainWindowColor);
            else if (R.string.color_Grey == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeGreyNoChangeMainWindowColor);
            else if (R.string.color_Carmine == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeCarmineNoChangeMainWindowColor);
            else if (R.string.color_Amber == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeAmberNoChangeMainWindowColor);
            else if (R.string.color_DarkBlue1 == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeDarkBlueNoChangeMainWindowColor);
            else if (R.string.color_Sandalwood == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeSandalwoodNoChangeMainWindowColor);
            else if (R.string.color_BambooGreen == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeBambooGreenNoChangeMainWindowColor);
            else if (R.string.color_BlueGrey == MusicUtils.getInstance().themeName)
                setTheme(R.style.DefaultAppThemeBlueGreyNoChangeMainWindowColor);
        }
        MobclickAgent.openActivityDurationTrack(false);

        if (application == null) {
            // 得到Application对象
            application = (MyApplication) getApplication();
        }
        oContext = this;// 把当前的上下文对象赋值给BaseActivity
        addActivity();// 调用添加方法


    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    // 添加Activity方法
    public void addActivity() {
        application.addActivity_(oContext);// 调用myApplication的添加Activity方法
    }
    //销毁当个Activity方法
    public void removeActivity() {
        application.removeActivity_(oContext);// 调用myApplication的销毁单个Activity方法
    }
    //销毁所有Activity方法
    public void removeALLActivity() {
        application.removeALLActivity_();// 调用myApplication的销毁所有Activity方法
    }
}

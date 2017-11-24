package ironbear775.com.musicplayer.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Calendar;

import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.Util.MusicUtils;

/**
 * Created by ironbear on 2016/11/7.
 */

public class BaseActivity extends AppCompatActivity {
    public static boolean isNight = false;
    public static boolean changeMainWindow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        isNight = sp.getBoolean("isNight", false);
        changeMainWindow = sp.getBoolean("changeMainWindow", true);
        MusicUtils.themeName = sp.getInt("themeName", R.string.color_Pink);
        MusicUtils.autoSwitchNightMode = sp.getBoolean("autoSwitchNightMode", true);
        boolean isManual = sp.getBoolean("isManual", false);

        long time = System.currentTimeMillis();

        final Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(time);
        int mHour = mCalendar.get(Calendar.HOUR_OF_DAY);

        if (MusicUtils.autoSwitchNightMode && !isManual) {
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
            if (R.string.color_Blue == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeBlue);
            else if (R.string.color_Green == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeGreen);
            else if (R.string.color_Indigo == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeIndigo);
            else if (R.string.color_Pink == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemePink);
            else if (R.string.color_Purple == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemePurple);
            else if (R.string.color_Red == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeRed);
            else if (R.string.color_LightBlue == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeLightBlue);
            else if (R.string.color_Teal == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeTeal);
            else if (R.string.color_Lime == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeLime);
            else if (R.string.color_Orange == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeOrange);
            else if (R.string.color_DeepOrange == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeDeepOrange);
            else if (R.string.color_Brown == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeBrown);
            else if (R.string.color_Grey == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeGrey);
            else if (R.string.color_Carmine == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeCarmine);
            else if (R.string.color_Amber == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeAmber);
            else if (R.string.color_DarkBlue1 == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeDarkBlue);
            else if (R.string.color_Sandalwood == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeSandalwood);
            else if (R.string.color_BambooGreen == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeBambooGreen);
            else if (R.string.color_BlueGrey == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeBlueGrey);
        }else {
            if (R.string.color_Blue == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeBlueNoChangeMainWindowColor);
            else if (R.string.color_Green == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeGreenNoChangeMainWindowColor);
            else if (R.string.color_Indigo == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeIndigoNoChangeMainWindowColor);
            else if (R.string.color_Pink == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemePinkNoChangeMainWindowColor);
            else if (R.string.color_Purple == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemePurpleNoChangeMainWindowColor);
            else if (R.string.color_Red == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeRedNoChangeMainWindowColor);
            else if (R.string.color_LightBlue == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeLightBlueNoChangeMainWindowColor);
            else if (R.string.color_Teal == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeTealNoChangeMainWindowColor);
            else if (R.string.color_Lime == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeLimeNoChangeMainWindowColor);
            else if (R.string.color_Orange == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeOrangeNoChangeMainWindowColor);
            else if (R.string.color_DeepOrange == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeDeepOrangeNoChangeMainWindowColor);
            else if (R.string.color_Brown == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeBrownNoChangeMainWindowColor);
            else if (R.string.color_Grey == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeGreyNoChangeMainWindowColor);
            else if (R.string.color_Carmine == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeCarmineNoChangeMainWindowColor);
            else if (R.string.color_Amber == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeAmberNoChangeMainWindowColor);
            else if (R.string.color_DarkBlue1 == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeDarkBlueNoChangeMainWindowColor);
            else if (R.string.color_Sandalwood == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeSandalwoodNoChangeMainWindowColor);
            else if (R.string.color_BambooGreen == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeBambooGreenNoChangeMainWindowColor);
            else if (R.string.color_BlueGrey == MusicUtils.themeName)
                setTheme(R.style.DefaultAppThemeBlueGreyNoChangeMainWindowColor);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

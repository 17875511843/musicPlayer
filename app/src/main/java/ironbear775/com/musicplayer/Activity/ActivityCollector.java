package ironbear775.com.musicplayer.Activity;

import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;

public class ActivityCollector {
    public static HashMap<Class<?>,AppCompatActivity> activities = new HashMap<>();

    public static void addActivity(AppCompatActivity activity,Class<?> clz){
        activities.put(clz,activity);
    }

    public static <T extends AppCompatActivity> boolean isActivityExist(Class<?> clz) {
        AppCompatActivity activity = activities.get(clz);

        return activity != null && !activity.isFinishing() && !activity.isDestroyed();
    }

}

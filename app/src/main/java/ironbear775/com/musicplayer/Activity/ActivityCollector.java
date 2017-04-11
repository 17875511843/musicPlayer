package ironbear775.com.musicplayer.Activity;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ironbear on 2016/11/7.
 */

public class ActivityCollector{
    private static final List<Activity> activities = new ArrayList<>();
    static void  addActivity(Activity activity){
        activities.add(activity);
    }
    static void removeActivity(Activity activity){
        activities.remove(activity);
    }
    public static void  finishAll(){
        for(Activity activity : activities){
            if(!activity.isFinishing()){
                activity.finish();
            }
        }
    }
}

package ironbear775.com.musicplayer;

import android.app.Application;

import com.umeng.commonsdk.UMConfigure;


/**
 * Created by ironbear on 2017/7/8.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UMConfigure.init(this,"5af7b675a40fa375ab0000de","CoolApk",UMConfigure.DEVICE_TYPE_PHONE,null);
        UMConfigure.setLogEnabled(true);
        UMConfigure.setEncryptEnabled(true);
    }

}


package ironbear775.com.musicplayer.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import ironbear775.com.musicplayer.R;
import ironbear775.com.musicplayer.util.OpenSource;


/**
 * Created by ironbear on 2017/3/30.
 */

public class About extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        Toolbar toolbar = findViewById(R.id.about_toolbar);
        TextView version = findViewById(R.id.version_number);
        final TextView openSource = findViewById(R.id.open_source);


        toolbar.setTitle(getResources().getString(R.string.about));
        toolbar.setTitleTextColor(Color.WHITE);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        PackageManager packageManager = getPackageManager();
        String versionCode = null;
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(),0);
             versionCode = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        version.setText(versionCode);
        openSource.setOnClickListener(v -> {
            OpenSource openSourceDialog = new OpenSource(About.this);
            openSourceDialog.show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

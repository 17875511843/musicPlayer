package ironbear775.com.musicplayer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ironbear775.com.musicplayer.R;

/**
 * Created by ironbear on 2017/7/3.
 */

public class IntroDemoFragment extends android.support.v4.app.Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.intro_demo_layout,container,false);
    }
}
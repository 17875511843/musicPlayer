package ironbear775.com.musicplayer.Util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by ironb on 2017/11/22.
 */

public class FootBarRelativeLayout extends RelativeLayout {
    public FootBarRelativeLayout(Context context) {
        super(context);
    }

    public FootBarRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FootBarRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}

package ironbear775.com.musicplayer.Util.Lyric;

import android.content.Context;

/**
 * Created by hzwangchenyan on 2016/11/17.
 */
class LrcUtils {
    static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}

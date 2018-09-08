package ironbear775.com.musicplayer.util.Lyric;

import android.content.Context;

/**
 * Created by hzwangchenyan on 2016/11/17.
 */
class LrcUtils {
    //将dp之转换为px像素值
    static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //将sp之转换为px像素值
    static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}

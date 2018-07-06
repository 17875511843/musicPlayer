package ironbear775.com.musicplayer.Util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by ironbear on 2016/12/21.
 */

public class SquareImageView extends android.support.v7.widget.AppCompatImageView {
    private int lastX, lastY;
    private boolean isMoved;
    private Runnable longPressRunnable;

    //移动像素阈值
    private static final int TOUCH_SLOP = 20;


    private OnLongClickListener onLongClickListener;

    public SquareImageView(Context context) {
        super(context);

    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLongPressRunnable();
    }

    public void setOnViewLongClickListener(OnLongClickListener listener) {
        onLongClickListener = listener;
    }

    public void initLongPressRunnable() {
        longPressRunnable = new Runnable() {
            @Override
            public void run() {
                performLongClick();
                if (onLongClickListener != null)
                    onLongClickListener.OnLongClick();
            }
        };
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;
                boolean isReleased = false;
                isMoved = false;

                postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout());

                break;
            case MotionEvent.ACTION_MOVE:
                if (isMoved)
                    break;

                if (Math.abs(lastX - x) > TOUCH_SLOP || Math.abs(lastY - y) > TOUCH_SLOP) {
                    isMoved = true;
                    removeCallbacks(longPressRunnable);
                }

                break;
            case MotionEvent.ACTION_UP:
                removeCallbacks(longPressRunnable);
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public interface OnLongClickListener {
        void OnLongClick();
    }
}

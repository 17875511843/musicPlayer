package ironbear775.com.musicplayer.Util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

public class AutoResizeTextView extends android.support.v7.widget.AppCompatTextView {

    private Paint mTextPaint;
    private float mTextSize;

    public AutoResizeTextView(Context context){
        super(context);
    }
    public AutoResizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void refitText(String text,int textViewHeight){
        if (text==null || textViewHeight < 0)
            return;
        mTextPaint = new Paint();
        mTextPaint.set(this.getPaint());

        int availableTextHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        int availableTextWidth = getWidth() - getPaddingStart() - getPaddingEnd();
        Rect boundRect = new Rect();
        mTextPaint.getTextBounds(text,0,availableTextWidth,boundRect);
        mTextSize = getTextSize();
        int height = boundRect.height();
        while (height > availableTextHeight){
            mTextSize -= 1;
            mTextPaint.setTextSize(mTextSize);
            mTextPaint.getTextBounds(text,0,availableTextWidth,boundRect);
            height = boundRect.height();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        refitText(this.getText().toString(),this.getHeight());
    }
}
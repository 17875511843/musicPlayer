package ironbear775.com.musicplayer.util.Lyric;


import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import ironbear775.com.musicplayer.R;

/**
 * 歌词
 * Created by wcy on 2015/11/9.
 */
public class LrcView extends View {
    private List<LrcEntry> mLrcEntryList = new ArrayList<>();
    private TextPaint mPaint = new TextPaint();
    private float mTextSize;
    private float mDividerHeight;
    private long mAnimationDuration;
    private int mNormalColor;
    private int mCurrentColor;
    private String mLabel;
    private float mLrcPadding;
    private ValueAnimator mAnimator;
    private float mAnimateOffset;
    private long mNextTime = 0L;
    private int mCurrentLine = 0;
    private Object mFlag;

    public LrcView(Context context) {
        this(context, null);
    }

    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.LrcView);
        mTextSize = ta.getDimension(R.styleable.LrcView_lrcTextSize, LrcUtils.sp2px(getContext(), 12));
        mDividerHeight = ta.getDimension(R.styleable.LrcView_lrcDividerHeight, LrcUtils.dp2px(getContext(), 20));
        mAnimationDuration = ta.getInt(R.styleable.LrcView_lrcAnimationDuration, 1000);
        mAnimationDuration = (mAnimationDuration < 0) ? 1000 : mAnimationDuration;
        mNormalColor = ta.getColor(R.styleable.LrcView_lrcNormalTextColor, 0xFFFFFFFF);
        mCurrentColor = ta.getColor(R.styleable.LrcView_lrcCurrentTextColor, 0xFFFF4081);
        mLabel = ta.getString(R.styleable.LrcView_lrcLabel);
        mLabel = TextUtils.isEmpty(mLabel) ? "暂无歌词" : mLabel;
        mLrcPadding = ta.getDimension(R.styleable.LrcView_lrcPadding, 10);
        ta.recycle();

        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mTextSize);
        mPaint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initEntryList();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(0, mAnimateOffset);

        // 中心Y坐标
        float centerY = getHeight() / 2;

        mPaint.setColor(mCurrentColor);

        // 无歌词文件
        if (!hasLrc()) {
            @SuppressLint("DrawAllocation")
            StaticLayout staticLayout = new StaticLayout(mLabel, mPaint, (int) getLrcWidth(),
                    Layout.Alignment.ALIGN_CENTER, 1f, 0f, false);
            drawText(canvas, staticLayout, centerY - staticLayout.getLineCount() * mTextSize / 2);
            return;
        }

        // 画当前行
        float currY = centerY - mLrcEntryList.get(mCurrentLine).getTextHeight() / 2;
        drawText(canvas, mLrcEntryList.get(mCurrentLine).getStaticLayout(), currY);

        // 画当前行上面的
        mPaint.setColor(mNormalColor);
        float upY = currY;
        for (int i = mCurrentLine - 1; i >= 0; i--) {
            upY -= mDividerHeight + mLrcEntryList.get(i).getTextHeight();
            drawText(canvas, mLrcEntryList.get(i).getStaticLayout(), upY);

            if (upY <= 0) {
                break;
            }
        }

        // 画当前行下面的
        float downY = currY + mLrcEntryList.get(mCurrentLine).getTextHeight() + mDividerHeight;
        for (int i = mCurrentLine + 1; i < mLrcEntryList.size(); i++) {
            drawText(canvas, mLrcEntryList.get(i).getStaticLayout(), downY);

            if (downY + mLrcEntryList.get(i).getTextHeight() >= getHeight()) {
                break;
            }

            downY += mLrcEntryList.get(i).getTextHeight() + mDividerHeight;
        }
    }

    private void drawText(Canvas canvas, StaticLayout staticLayout, float y) {
        canvas.save();
        canvas.translate(mLrcPadding, y);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    private float getLrcWidth() {
        return getWidth() - mLrcPadding * 2;
    }

    /**
     * 设置歌词为空时屏幕中央显示的文字，如“暂无歌词”
     */
    public void setLabel(String label) {
        mLabel = label;
        postInvalidate();
    }

    /**
     * 加载歌词文件
     *
     * @param lrcFile 歌词文件
     */
    public void loadLrc(final File lrcFile) {
        reset();

        setFlag(lrcFile);
        @SuppressLint("StaticFieldLeak")
        AsyncTask<File, Integer, List<LrcEntry>> loadLrcTask = new AsyncTask<File, Integer, List<LrcEntry>>() {
            @Override
            protected List<LrcEntry> doInBackground(File... params) {
                return LrcEntry.parseLrc(params[0]);
            }

            @Override
            protected void onPostExecute(List<LrcEntry> lrcEntries) {
                if (getFlag() == lrcFile) {
                    onLrcLoaded(lrcEntries);
                    setFlag(null);
                }
            }
        };
        loadLrcTask.execute(lrcFile);
    }

    /**
     * 加载歌词文件
     *
     * @param lrcText 歌词文本
     */
    public void loadLrc(final String lrcText) {
        //重置操作
        reset();

        setFlag(lrcText);

        //异步加载歌词
        @SuppressLint("StaticFieldLeak")
        AsyncTask<String, Integer, List<LrcEntry>> loadLrcTask = new AsyncTask<String, Integer, List<LrcEntry>>() {
            @Override
            protected List<LrcEntry> doInBackground(String... params) {
                return LrcEntry.parseLrc(params[0]);
            }

            @Override
            protected void onPostExecute(List<LrcEntry> lrcEntries) {
                if (getFlag() == lrcText) {
                    //加载成功后将歌词加入到集合准备显示
                    onLrcLoaded(lrcEntries);
                    setFlag(null);
                }
            }
        };

        loadLrcTask.execute(lrcText);
    }

    private void onLrcLoaded(List<LrcEntry> entryList) {

        if (entryList != null && !entryList.isEmpty()) {
            LrcEntry next = null;
            ListIterator<LrcEntry> iterator = entryList.listIterator();
            if (entryList.size() > 2)
                next = iterator.next();

            //将时间轴相同的歌词合并显示，通常原歌词和翻译歌词具有相同的时间轴
            while (iterator.hasNext()) {
                LrcEntry current = next;
                next = iterator.next();
                if (current != null && current.getTime() == next.getTime()) {
                    current.setText(current.getText() + "\n" + next.getText());
                    iterator.remove();
                }
            }
            //加载排序合并完成好的歌词集合
            mLrcEntryList.addAll(entryList);
        }

        if (hasLrc()) {
            initEntryList();
            initNextTime();
        }
        //刷新UI重新绘制
        postInvalidate();
    }

    /**
     * 刷新歌词
     *
     * @param time 当前播放时间
     */
    public void updateTime(long time) {
        // 避免重复绘制
        if (time < mNextTime) {
            return;
        }
        for (int i = mCurrentLine; i < mLrcEntryList.size(); i++) {
            if (mLrcEntryList.get(i).getTime() > time) {
                mNextTime = mLrcEntryList.get(i).getTime();
                mCurrentLine = (i < 1) ? 0 : (i - 1);
                newlineOnUI(i);
                break;
            } else if (i == mLrcEntryList.size() - 1) {
                // 最后一行
                mCurrentLine = mLrcEntryList.size() - 1;
                mNextTime = Long.MAX_VALUE;
                newlineOnUI(i);
                break;
            }
        }
    }

    /**
     * 将歌词滚动到指定时间
     *
     * @param time 指定的时间
     */
    public void onDrag(long time) {
        for (int i = 0; i < mLrcEntryList.size(); i++) {
            if (mLrcEntryList.get(i).getTime() > time) {
                if (i == 0) {
                    mCurrentLine = i;
                    initNextTime();
                } else {
                    mCurrentLine = i - 1;
                    mNextTime = mLrcEntryList.get(i).getTime();
                }
                newlineOnUI(i);
                break;
            }
        }
    }

    /**
     * 歌词是否有效
     *
     * @return true，如果歌词有效，否则false
     */
    public boolean hasLrc() {
        return !mLrcEntryList.isEmpty();
    }

    private void reset() {
        mLrcEntryList.clear();
        mCurrentLine = 0;
        mNextTime = 0L;

        postInvalidate();
    }

    private void initEntryList() {
        if (getWidth() == 0) {
            return;
        }
        //排序
        Collections.sort(mLrcEntryList);

        for (LrcEntry lrcEntry : mLrcEntryList) {
            lrcEntry.init(mPaint, (int) getLrcWidth());
        }
    }

    private void initNextTime() {
        if (mLrcEntryList.size() > 1) {
            mNextTime = mLrcEntryList.get(1).getTime();
        } else {
            mNextTime = Long.MAX_VALUE;
        }
    }

    private void newlineOnUI(final int index) {
        post(() -> newlineAnimation(index));
    }

    /**
     * 换行动画<br>
     * 属性动画只能在主线程使用
     */
    private void newlineAnimation(int index) {
        stopAnimation();

        mAnimator = ValueAnimator.ofFloat(mLrcEntryList.get(index).getTextHeight() + mDividerHeight, 0.0f);
        mAnimator.setDuration(mAnimationDuration * mLrcEntryList.get(index).getStaticLayout().getLineCount());
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(animation -> {
            mAnimateOffset = (float) animation.getAnimatedValue();
            invalidate();
        });
        mAnimator.start();
    }

    private void stopAnimation() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.end();
        }
    }

    public Object getFlag() {
        return mFlag;
    }

    public void setFlag(Object flag) {
        this.mFlag = flag;
    }
}

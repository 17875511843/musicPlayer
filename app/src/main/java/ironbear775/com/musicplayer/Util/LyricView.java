package ironbear775.com.musicplayer.Util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class LyricView extends View {


    private int mBgCol;
    private int mCurrentTextSize;

    //存放开头那些没有时间的信息
    private List<String> info = new ArrayList<String>();

    public static ArrayList<Long> timeSpace = new ArrayList<Long>();

    //存放有时间的歌词
    private Map<String, String> lrcs = new Hashtable<String, String>();
    //存放按照从小到大排序好的时间信息
    private Object[] arr;

    private Long[] time;

    //不高亮的歌词画笔
    private Paint mLoseFocusPaint;
    //高亮的
    private Paint mOnFocusePaint;
    //一行歌词的开始位置X
    private float drawTextX = 0;
    //Y
    private float drawTextY = 0;
    //整个View的高
    private float viewHeight = 0;
    //间隔，移动的大小
    private int mSpacing;
    //高亮的行数
    private int mIndex = 0;


    //获取数据源，接口
    public void setLrcSource(String path,int isFileOrString) {
        if (isFileOrString == 1) {
            try {
                FileInputStream reader;
                reader = new FileInputStream(path);
                InputStreamReader isr = new InputStreamReader(reader);

                clear();
                getLrcs(isr, null);
                for (int i = 0; i < lrcs.size() - 1; i++){
                    timeSpace.add(i, parseTime(arr[i + 1].toString()) - parseTime(arr[i].toString()));
                    Log.d("time",i+""+timeSpace.get(i));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else {
            clear();
            getLrcs(null,path);
            for (int i = 0;i<lrcs.size()-1;i++) {
                timeSpace.add(i, parseTime(arr[i + 1].toString()) - parseTime(arr[i].toString()));
            }
        }
    }

    //获取当前行位置，接口
    public void setLrcPostion(long position) {

        if (mIndex < lrcs.size()) {

            for (int i = 0; i < lrcs.size() - 1; i++) {
                if (position <= time[0]) {
                    mIndex = 0;
                }else if (time[i] < position && position < time[i + 1]) {
                    mIndex = i;
                }
            }

            postInvalidate();

            Log.d("Position",position+"");
            Log.d("POS",mIndex+"");
        }

    }


    //构造方法
    public LyricView(Context context) {
        this(context, null);
    }

    public LyricView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mLoseFocusPaint = new Paint();
        mLoseFocusPaint.setAntiAlias(true);
        mLoseFocusPaint.setTextSize(30);
        mLoseFocusPaint.setColor(Color.CYAN);
        mLoseFocusPaint.setTypeface(Typeface.DEFAULT);

        mOnFocusePaint = new Paint();
        mOnFocusePaint.setAntiAlias(true);
        mOnFocusePaint.setColor(Color.WHITE);
        mOnFocusePaint.setTextSize(40);
        mOnFocusePaint.setTypeface(Typeface.DEFAULT_BOLD);

    }

    public void clear(){
        lrcs.clear();
        info.clear();
        timeSpace.clear();
        arr = null;
        mIndex = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //从中间开始，而且歌词居中。在下面设置了
        drawTextX = w * 0.5f;
        viewHeight = h;
        //从0.3高度的地方开始画
        drawTextY = h * 0.5f;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(mBgCol);

        Paint p = mLoseFocusPaint;
        p.setTextAlign(Paint.Align.CENTER);
        Paint p2 = mOnFocusePaint;
        p2.setTextAlign(Paint.Align.CENTER);


        mSpacing = mCurrentTextSize + 70;

        //画高亮的
        canvas.drawText(lrcs.get(arr[mIndex]), drawTextX, drawTextY, p2);

        //画高亮上面的歌词，高度递减，透明度递减
        int alphaValue = 25;
        float tempY = drawTextY;
        for (int i = mIndex - 1; i >= 0; i--) {
            tempY -= mSpacing;
            if (tempY < 0) {
                break;
            }
            p.setColor(Color.argb(255 - alphaValue, 255, 255, 255));
            canvas.drawText(lrcs.get(arr[i]), drawTextX, tempY, p);
            alphaValue += 25;
        }

        //画高亮下面的歌词，高度递增，透明度递减
        alphaValue = 25;
        tempY = drawTextY;
        for (int i = mIndex + 1; i < lrcs.size(); i++) {

            tempY += mSpacing;
            //超出不显示啦
            if (tempY > viewHeight) {
                break;
            }

            p.setColor(Color.argb(255 - alphaValue, 245, 245, 245));
            canvas.drawText(lrcs.get(arr[i]), drawTextX, tempY, p);


            //如果没超出就达到了100%透明，往后的都100%透明
            if (alphaValue + 25 > 255) {

                alphaValue = 255;

            } else {

                alphaValue += 25;
            }
        }

        //准备下一行刷新，重绘，这有赖于传进来的时间对比

        mIndex++;
    }


    private void getLrcs(@Nullable InputStreamReader isr,@Nullable String path) {

        if (path!=null){
            BufferedReader reader = new BufferedReader(new StringReader(path));
            String line;

            try {
                while ((line = reader.readLine()) != null) {

                    decodeLrc(line);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //key键的时间排序、组合
            convertArrays();

        }else {
            try {

                BufferedReader reader = new BufferedReader(isr);
                String line;

                while ((line = reader.readLine()) != null) {

                    decodeLrc(line);

                }

                //key键的时间排序、组合
                convertArrays();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



    //获取歌词，放进集合中
    public void decodeLrc(String str) {


        if (str.startsWith("[ti:")) {
            info.add(str.substring(4, str.lastIndexOf("]")));

        } else if (str.startsWith("[ar:")) {

            info.add(str.substring(4, str.lastIndexOf("]")));

        } else if (str.startsWith("[al:")) {

            info.add(str.substring(4, str.lastIndexOf("]")));
        } else if (str.startsWith("[la:")) {
            info.add(str.substring(4, str.lastIndexOf("]")));

        } else if (str.startsWith("[by:")) {
            info.add(str.substring(4, str.lastIndexOf("]")));

        } else {

            int startIndex;
            int tempIndex = -1;
            //获取多个中括号的相同歌词的信息
            while ((startIndex = str.indexOf("[", tempIndex + 1)) != -1) {

                int endIndex = str.indexOf("]", tempIndex + 1);
                String tempTime = str.substring(tempIndex + 2, endIndex);
                lrcs.put(tempTime, str.substring(str.lastIndexOf("]") + 1, str.length()));

                tempIndex = endIndex;

            }
        }
    }

    // 解析时间，把时间转为long
    @Nullable
    private Long parseTime(String time) {

        if (time.indexOf(":") != -1) {

            String[] min = time.split(":");
            String[] sec = min[1].split("\\.");

            long minInt = Long.parseLong(min[0]
                    .replaceAll("\\D+", "")
                    .replaceAll("\r", "")
                    .replaceAll("\n", "")
                    .trim());
            long secInt = Long.parseLong(sec[0]
                    .replaceAll("\\D+", "")
                    .replaceAll("\r", "")
                    .replaceAll("\n", "")
                    .trim());
            long milInt = Long.parseLong(sec[1]
                    .replaceAll("\\D+", "")
                    .replaceAll("\r", "")
                    .replaceAll("\n", "")
                    .trim());

            return minInt * 60 * 1000 + secInt * 1000 + milInt * 10;
        } else {

            return null;

        }

    }


    //把hashtable转为有秩序的组合
    public void convertArrays() {
        arr = lrcs.keySet().toArray();
        Arrays.sort(arr);
        time = new Long[arr.length];
        for (int i = 0;i< time.length;i++){
            time[i] = parseTime(arr[i].toString());
            Log.d("time",i+""+ time[i]);
        }
    }
}
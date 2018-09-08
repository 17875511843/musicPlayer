package ironbear775.com.musicplayer.util.Lyric;

import android.support.annotation.NonNull;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LrcEntry implements Comparable<LrcEntry> {
    private long time; //该行歌词的时间轴信息
    private String text; //歌词本体
    private StaticLayout staticLayout;
    private TextPaint paint;

    private LrcEntry(long time, String text) {
        this.time = time;
        this.text = text;
    }

    void init(TextPaint paint, int width) {
        this.paint = paint;
        staticLayout = new StaticLayout(text, paint, width, Layout.Alignment.ALIGN_CENTER, 1f, 0f, false);
    }

    long getTime() {
        return time;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    StaticLayout getStaticLayout() {
        return staticLayout;
    }

    float getTextHeight() {
        if (paint == null || staticLayout == null) {
            return 0;
        }
        return staticLayout.getLineCount() * paint.getTextSize();
    }

    //重写排序的规则，这里通过时间轴的时间长短进行排序
    @Override
    public int compareTo(@NonNull LrcEntry entry) {
        return (int) (time - entry.getTime());
    }

    //解析lrc文件
    static List<LrcEntry> parseLrc(File lrcFile) {
        if (lrcFile == null || !lrcFile.exists()) {
            return null;
        }

        List<LrcEntry> entryList = new ArrayList<>();
        try {
            //逐行读取文本并添加
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(lrcFile), "utf-8"));
            String line;
            while ((line = br.readLine()) != null) {
                //将一行歌词解析为时间轴和歌词本体
                List<LrcEntry> list = parseLine(line);
                if (list != null && !list.isEmpty()) {
                    entryList.addAll(list);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(entryList);

        return entryList;
    }

    //解析lrc文本
    static List<LrcEntry> parseLrc(String lrcText) {
        if (TextUtils.isEmpty(lrcText)) {
            return null;
        }

        List<LrcEntry> entryList = new ArrayList<>();
        //通过\n识别不同行歌词
        String[] array = lrcText.split("\\n");
        for (String line : array) {
            List<LrcEntry> list = parseLine(line);
            if (list != null) {
                entryList.addAll(list);
            }
        }

        //对列表集合进行排序，这里的entryList实现了Comparable排序接口，才能做到自定义排序
        Collections.sort(entryList);
        return entryList;
    }

    //解析一行歌词
    private static List<LrcEntry> parseLine(String line) {
        if (TextUtils.isEmpty(line)) {
            return null;
        }
        int type; //歌词类型识别码
        line = line.trim();
        //匹配歌词格式为[00:00.000]....的歌词
        Matcher newLineMatcher = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d\\d\\d])+)(.+)").matcher(line);
        //匹配歌词格式为[00:00.00]....的歌词
        Matcher lineMatcher = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d\\d])+)(.+)").matcher(line);
        //匹配歌词格式为[00:00：000]....的歌词
        Matcher newLineMatcher1 = Pattern.compile("((\\[\\d\\d:\\d\\d:\\d\\d\\d])+)(.+)").matcher(line);
        //匹配歌词格式为[00:00：00]....的歌词
        Matcher lineMatcher1 = Pattern.compile("((\\[\\d\\d:\\d\\d:\\d\\d])+)(.+)").matcher(line);

        if (newLineMatcher.matches())
            type = 1;
        else if (lineMatcher.matches())
            type = 2;
        else if (newLineMatcher1.matches())
            type = 3;
        else if (lineMatcher1.matches())
            type = 4;
        else {
            return null;
        }

        String times, text;

        List<LrcEntry> entryList = new ArrayList<>();

        if (type == 1) {
            times = newLineMatcher.group(1); //0.整个字符串，1.第一个括号里的时间轴字符串
            text = newLineMatcher.group(3); //3为时间轴后的歌词本体

            //匹配[00:00.000]样式时间轴
            Matcher newTimeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d\\d\\d)]").matcher(times);
            while (newTimeMatcher.find()) {
                long min = Long.parseLong(newTimeMatcher.group(1)); //截取分钟
                long sec = Long.parseLong(newTimeMatcher.group(2)); //截取秒钟
                long mil = Long.parseLong(newTimeMatcher.group(3)); //截取毫秒
                //转化为毫秒
                long time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil;
                //将该歌词添加到歌词列表中
                entryList.add(new LrcEntry(time, text));
            }

        } else if (type == 2) {
            times = lineMatcher.group(1);
            text = lineMatcher.group(3);

            //匹配[00:00.00]样式时间轴
            Matcher timeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d\\d)]").matcher(times);
            while (timeMatcher.find()) {
                long min = Long.parseLong(timeMatcher.group(1));
                long sec = Long.parseLong(timeMatcher.group(2));
                long mil = Long.parseLong(timeMatcher.group(3));

                long time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil * 10;
                entryList.add(new LrcEntry(time, text));
            }

        } else if (type == 3) {
            times = newLineMatcher1.group(1);
            text = newLineMatcher1.group(3);

            //匹配[00:00：000]样式时间轴
            Matcher newTimeMatcher1 = Pattern.compile("\\[(\\d\\d):(\\d\\d):(\\d\\d\\d)]").matcher(times);
            while (newTimeMatcher1.find()) {
                long min = Long.parseLong(newTimeMatcher1.group(1));
                long sec = Long.parseLong(newTimeMatcher1.group(2));
                long mil = Long.parseLong(newTimeMatcher1.group(3));

                long time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil;
                entryList.add(new LrcEntry(time, text));
            }

        } else {
            times = lineMatcher1.group(1);
            text = lineMatcher1.group(3);

            //匹配[00:00：00]样式时间轴
            Matcher timeMatcher1 = Pattern.compile("\\[(\\d\\d):(\\d\\d):(\\d\\d)]").matcher(times);

            while (timeMatcher1.find()) {
                long min = Long.parseLong(timeMatcher1.group(1));
                long sec = Long.parseLong(timeMatcher1.group(2));
                long mil = Long.parseLong(timeMatcher1.group(3));

                long time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil * 10;
                entryList.add(new LrcEntry(time, text));
            }

        }

        return entryList;
    }
}

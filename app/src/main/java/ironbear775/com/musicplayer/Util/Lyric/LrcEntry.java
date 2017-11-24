package ironbear775.com.musicplayer.Util.Lyric;

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
    private long time;
    private String text;
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

    StaticLayout getStaticLayout() {
        return staticLayout;
    }

    float getTextHeight() {
        if (paint == null || staticLayout == null) {
            return 0;
        }
        return staticLayout.getLineCount() * paint.getTextSize();
    }

    @Override
    public int compareTo(@NonNull LrcEntry entry) {
        return (int) (time - entry.getTime());
    }

    static List<LrcEntry> parseLrc(File lrcFile) {
        if (lrcFile == null || !lrcFile.exists()) {
            return null;
        }

        List<LrcEntry> entryList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(lrcFile), "utf-8"));
            String line;
            while ((line = br.readLine()) != null) {
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

    static List<LrcEntry> parseLrc(String lrcText) {
        if (TextUtils.isEmpty(lrcText)) {
            return null;
        }

        List<LrcEntry> entryList = new ArrayList<>();
        String[] array = lrcText.split("\\n");
        for (String line : array) {
            List<LrcEntry> list = parseLine(line);
            if (list != null) {
                entryList.addAll(list);
            }
        }

        Collections.sort(entryList);
        return entryList;
    }

    private static List<LrcEntry> parseLine(String line) {
        if (TextUtils.isEmpty(line)) {
            return null;
        }
        int twoOrThree;
        line = line.trim();
        Matcher newLineMatcher = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d\\d\\d])+)(.+)").matcher(line);
        Matcher lineMatcher = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d\\d])+)(.+)").matcher(line);
        Matcher newLineMatcher1 = Pattern.compile("((\\[\\d\\d:\\d\\d:\\d\\d\\d])+)(.+)").matcher(line);
        Matcher lineMatcher1 = Pattern.compile("((\\[\\d\\d:\\d\\d:\\d\\d])+)(.+)").matcher(line);

        if (newLineMatcher.matches())
            twoOrThree = 1;
        else if (lineMatcher.matches())
            twoOrThree = 2;
        else if (newLineMatcher1.matches())
            twoOrThree = 3;
        else if (lineMatcher1.matches())
            twoOrThree = 4;
        else {
            return null;
        }

        String times, text;

        List<LrcEntry> entryList = new ArrayList<>();

        if (twoOrThree == 1) {
            times = newLineMatcher.group(1);//0.整个字符串，1.第一个括号里的字符串
            text = newLineMatcher.group(3);

            Matcher newTimeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d\\d\\d)]").matcher(times);
            while (newTimeMatcher.find()) {
                long min = Long.parseLong(newTimeMatcher.group(1));
                long sec = Long.parseLong(newTimeMatcher.group(2));
                long mil = Long.parseLong(newTimeMatcher.group(3));

                long time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil;
                entryList.add(new LrcEntry(time, text));
            }

        } else if (twoOrThree == 2) {
            times = lineMatcher.group(1);
            text = lineMatcher.group(3);

            Matcher timeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d\\d)]").matcher(times);
            while (timeMatcher.find()) {
                long min = Long.parseLong(timeMatcher.group(1));
                long sec = Long.parseLong(timeMatcher.group(2));
                long mil = Long.parseLong(timeMatcher.group(3));

                long time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil * 10;
                entryList.add(new LrcEntry(time, text));
            }

        } else if (twoOrThree == 3) {
            times = newLineMatcher1.group(1);
            text = newLineMatcher1.group(3);

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

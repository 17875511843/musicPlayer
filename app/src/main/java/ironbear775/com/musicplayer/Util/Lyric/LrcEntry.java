package ironbear775.com.musicplayer.Util.Lyric;

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
    public int compareTo(LrcEntry entry) {
        if (entry == null) {
            return -1;
        }
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
            if (list != null && !list.isEmpty()) {
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
        int twoOrThree = 2;
        line = line.trim();
        Matcher lineMatcher = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d\\d\\])+)(.+)").matcher(line);
        Matcher newLineMatcher = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d\\d\\d\\])+)(.+)").matcher(line);
        if (!lineMatcher.matches()) {
            twoOrThree = 3;
            if (!newLineMatcher.matches()){
                twoOrThree = 2;
                return null;
            }

        }
        String times,text;
        if (twoOrThree == 2) {
            times = lineMatcher.group(1);
            text = lineMatcher.group(3);
        }else {
            times = newLineMatcher.group(1);
            text = newLineMatcher.group(3);
        }
        List<LrcEntry> entryList = new ArrayList<>();

        Matcher timeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d\\d)\\]").matcher(times);

        while (timeMatcher.find()) {
            long min = Long.parseLong(timeMatcher.group(1));
            long sec = Long.parseLong(timeMatcher.group(2));
            long mil = Long.parseLong(timeMatcher.group(3));

            long time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil * 10;
            entryList.add(new LrcEntry(time, text));
        }

        Matcher newTimeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d\\d\\d)\\]").matcher(times);
        while (newTimeMatcher.find()) {
            long min = Long.parseLong(newTimeMatcher.group(1));
            long sec = Long.parseLong(newTimeMatcher.group(2));
            long mil = Long.parseLong(newTimeMatcher.group(3));

            long time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil;
            entryList.add(new LrcEntry(time, text));
        }
        return entryList;
    }
}
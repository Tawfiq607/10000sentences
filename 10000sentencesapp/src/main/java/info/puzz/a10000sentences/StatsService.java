package info.puzz.a10000sentences;

import android.content.Intent;

import com.activeandroid.Model;
import com.activeandroid.query.Select;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.puzz.a10000sentences.models.SentenceHistory;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

public final class StatsService {

    @Data
    @Accessors(chain = true)
    @ToString
    public static class Stats {
        DataPoint[] timePerDay;
        DataPoint[] donePerDay;
    }

    public StatsService() throws Exception {
        throw new Exception();
    }

    public static Stats getStats(int daysAgo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -daysAgo);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        List<SentenceHistory> history = new Select()
                .from(SentenceHistory.class)
                .where("created>?", cal.getTime().getTime())
                .orderBy("created")
                .execute();


        Map<Long, List<Integer>> timeByDay = new HashMap<>();
        Map<Long, Map<String, Integer>> doneByDay = new HashMap<>();

        for (SentenceHistory model : history) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(model.created);
            c.set(Calendar.HOUR_OF_DAY, 12);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.set(Calendar.DST_OFFSET, 0);
            c.set(Calendar.ZONE_OFFSET, 0);
            long time = c.getTimeInMillis();
            if (!timeByDay.containsKey(time)) {
                timeByDay.put(time, new ArrayList<Integer>());
                doneByDay.put(time, new HashMap<String, Integer>());
            }
            timeByDay.get(time).add(model.time);
            doneByDay.get(time).put(model.collectionId, model.doneCount);
        }

        List<DataPoint> timeDailyData = new ArrayList<>();
        for (Map.Entry<Long, List<Integer>> e : timeByDay.entrySet()) {
            timeDailyData.add(new DataPoint(e.getKey(), sum(e.getValue())));
        }
        
        List<DataPoint> doneDailyData = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Integer>> e : doneByDay.entrySet()) {
            int sum = 0;
            for (Integer integer : e.getValue().values()) {
                sum += integer.intValue();
            }
            doneDailyData.add(new DataPoint(e.getKey(), sum));
        }

        return new Stats()
                .setTimePerDay(timeDailyData.toArray(new DataPoint[timeDailyData.size()]))
                .setDonePerDay(doneDailyData.toArray(new DataPoint[doneDailyData.size()]));
    }

    private static final float avg(List<Integer> l) {
        if (l.size() == 0) {
            return 0;
        }

        return sum(l) + ((float) l.size());
    }

    private static float sum(List<Integer> l) {
        int s = 0;
        for (Integer i : l) {
            s += i.intValue();
        }
        return s;
    }

}

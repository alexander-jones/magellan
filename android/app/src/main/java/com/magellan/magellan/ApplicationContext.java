package com.magellan.magellan;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.DashPathEffect;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.magellan.magellan.quote.IQuoteService;
import com.magellan.magellan.service.alphavantage.AlphaVantageService;
import com.magellan.magellan.stock.Stock;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.chrono.GregorianChronology;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

public class ApplicationContext {

    private static String PREFERENCE_KEY = "APPLICATION_CONTEXT";

    private static Context mContext;
    private static int mWatchListGeneration = 0;
    private static List<Stock> mWatchList = null;
    private static SharedPreferences mSharedPreferences;
    private static IQuoteService mQuoteService = new AlphaVantageService();

    public static IQuoteService getQuoteService()
    {
        return mQuoteService;
    }
    public static DateTimeZone getTradingTimeZone() { return DateTimeZone.forID("America/New_York"); }
    public static final GregorianChronology getChronology() {return  GregorianChronology.getInstance(getTradingTimeZone());}

    public static enum StockMarketHolidays2017
    {
        NewYearsDay(1, 2), // actually 1,1 but observed 1, 2
        MartinLutherKingDay(1, 16),
        WashingtonsBirthday(2, 20),
        GoodFriday(4, 14),
        MemorialDay(5, 29),
        IndependenceDay(7,4),
        LaborDay(9,4),
        ThanskgivingDay(11, 23),
        Christmas(12, 25);

        private final DateTime date;
        StockMarketHolidays2017(int month, int day)
        {
            date = new DateTime(2017, month, day, 0, 0, 0, 0, getChronology());
        }
    }

    public static enum StockMarketHolidays2018
    {
        NewYearsDay(1, 1),
        MartinLutherKingDay(1, 15),
        WashingtonsBirthday(2, 19),
        GoodFriday(3, 30),
        MemorialDay(5, 28),
        IndependenceDay(7,4),
        LaborDay(9,3),
        ThanskgivingDay(11, 22),
        Christmas(12, 25);

        public final DateTime date;
        StockMarketHolidays2018(int month, int day)
        {
            date = new DateTime(2018, month, day, 0, 0, 0, 0, getChronology());
        }
    }

    public static enum StockMarketHolidays2019
    {
        NewYearsDay(1, 1),
        MartinLutherKingDay(1, 21),
        WashingtonsBirthday(2, 18),
        GoodFriday(4, 19),
        MemorialDay(5, 27),
        IndependenceDay(7,4),
        LaborDay(9,2),
        ThanskgivingDay(11, 28),
        Christmas(12, 25);

        public final DateTime date;
        StockMarketHolidays2019(int month, int day)
        {
            date = new DateTime(2019, month, day, 0, 0, 0, 0, getChronology());
        }
    }

    // initialize default settins for any charts in this activity
    public static void initializeSimpleChart(CombinedChart chart)
    {
        chart.setNoDataText("");
        chart.setTouchEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawMarkers(false);
        chart.setDrawBorders(false);
        chart.getDescription().setText("");
        chart.getLegend().setEnabled(false);
        chart.setViewPortOffsets(10, 10, 10, 10);

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawLabels(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceMin(0.0f);
        xAxis.setSpaceMax(0.0f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceBottom(0);
        leftAxis.setSpaceTop(0);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setSpaceBottom(0);
        rightAxis.setSpaceTop(0);
    }

    public static DateTime getLastTradingDayCloseTime()
    {
        DateTime ret = DateTime.now(getChronology());
        boolean correctedForEndOfDay = false;
        boolean checkIfTradingDay = true;
        while (checkIfTradingDay)
        {
            checkIfTradingDay = false;

            DateTime currentTradingDate = ret.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

            int year = ret.year().get();
            if (year == 2017)
            {
                for (StockMarketHolidays2017 holiday : StockMarketHolidays2017.values())
                {
                    if (holiday.date.isEqual(currentTradingDate))
                    {
                        ret = getDayEndOfClose(ret.minusDays(1));
                        checkIfTradingDay = correctedForEndOfDay = true;
                        break;
                    }
                }
            }
            else if (year == 2018)
            {
                for (StockMarketHolidays2018 holiday : StockMarketHolidays2018.values())
                {
                    if (holiday.date.isEqual(currentTradingDate))
                    {
                        ret = getDayEndOfClose(ret.minusDays(1));
                        checkIfTradingDay = correctedForEndOfDay = true;
                        break;
                    }
                }
            }
            else if (year == 2019)
            {
                for (StockMarketHolidays2019 holiday : StockMarketHolidays2019.values())
                {
                    if (holiday.date.isEqual(currentTradingDate))
                    {
                        ret = getDayEndOfClose(ret.minusDays(1));
                        checkIfTradingDay = correctedForEndOfDay =  true;
                        break;
                    }
                }
            }
            else
                Log.e("Magellan", "ERROR: Holiday / Blacklist days not handed for year " + Integer.toString(year));

            int hourOfDay = ret.hourOfDay().get();
            int minuteOfDay = ret.minuteOfDay().get();

            int endDay = ret.dayOfWeek().get();
            if (endDay == 6)
            {
                ret = ret.minus(Duration.standardDays(1));
                checkIfTradingDay = true;
            }
            else if (endDay == 7)
            {
                ret = ret.minus(Duration.standardDays(2));
                checkIfTradingDay = true;
            }
            else if (hourOfDay < 9 || ( hourOfDay == 9  && minuteOfDay < 40)) // make sure we have at least 2 quotes (as line graphs will not work with one entry point)
            {
                checkIfTradingDay = true;

                if (endDay == 1)
                    ret = ret.minus(Duration.standardDays(3));
                else
                    ret = ret.minus(Duration.standardDays(1));
            }

            if (!correctedForEndOfDay)
                ret = ret.withHourOfDay(16).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0); // 4:00 pm is NYSE close

        }

        return ret;
    }

    public static DateTime getOpenTimeForCloseTime(DateTime closeTime)
    {
        return closeTime.minusHours(7).plusMinutes(30); // 9:30 EST is NYSE open*/
    }

    public static DateTime getCloseTimeOneTradingWeekFromClose(DateTime closeTime)
    {
        DateTime ret = closeTime.minusDays(1);
        int tradingDaysPassed = 1;
        while (tradingDaysPassed <  6)
        {
            boolean isTradingDay = true;
            int endDay = ret.dayOfWeek().get();
            if (endDay == 6)
            {
                ret = ret.minus(Duration.standardDays(1));
                isTradingDay = false;
            }
            else if (endDay == 7)
            {
                ret = ret.minus(Duration.standardDays(2));
                isTradingDay = false;
            }
            else
            {
                DateTime currentTradingDate = ret.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

                int year = ret.year().get();
                if (year == 2017) {
                    for (StockMarketHolidays2017 holiday : StockMarketHolidays2017.values()) {
                        if (holiday.date.isEqual(currentTradingDate)) {
                            ret = getDayEndOfClose(ret.minusDays(1));
                            isTradingDay = false;
                            break;
                        }
                    }
                } else if (year == 2018) {
                    for (StockMarketHolidays2018 holiday : StockMarketHolidays2018.values()) {
                        if (holiday.date.isEqual(currentTradingDate)) {
                            ret = getDayEndOfClose(ret.minusDays(1));
                            isTradingDay = false;
                            break;
                        }
                    }
                } else if (year == 2019) {
                    for (StockMarketHolidays2019 holiday : StockMarketHolidays2019.values()) {
                        if (holiday.date.isEqual(currentTradingDate)) {
                            ret = getDayEndOfClose(ret.minusDays(1));
                            isTradingDay = false;
                            break;
                        }
                    }
                } else
                    Log.e("Magellan", "ERROR: Holiday / Blacklist days not handed for year " + Integer.toString(year));
            }

            if (isTradingDay)
            {
                ret = ret.minusDays(1);
                ++tradingDaysPassed;
            }
        }
        return ret;
    }

    public static void init(Context context)
    {
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        mWatchList = Stock.loadFrom(mSharedPreferences);
        mWatchListGeneration = mSharedPreferences.getInt("GENERATION", 0);
        if (mWatchList == null){
            mWatchList = new ArrayList<Stock>();
        }
    }

    public static int getWatchListGeneration() { return mWatchListGeneration;}

    public static int getWatchListIndex(Stock stock)
    {
        int index = -1;
        for (int i =0; i < mWatchList.size(); ++i){
            if (stock.equals(mWatchList.get(i)))
            {
                index = i;
                break;
            }
        }
        return index;
    }

    public static boolean moveItemInWatchlist(int from, int to)
    {
        if (from < 0 || from >= mWatchList.size())
            return false;

        if (to < 0 || to >= mWatchList.size())
            return false;

        ++mWatchListGeneration;
        mWatchList.add(to, mWatchList.remove(from));
        saveWatchList();
        return true;
    }

    public static boolean removeFromWatchList(int stock)
    {
        if (stock < 0 || stock >= mWatchList.size())
            return false;

        ++mWatchListGeneration;
        mWatchList.remove(stock);
        saveWatchList();
        return true;
    }

    public static boolean removeFromWatchList(Stock stock)
    {
        int indexToRemove = getWatchListIndex(stock);
        if (indexToRemove == -1)
            return false;

        removeFromWatchList(indexToRemove);
        return true;
    }

    public static boolean isStockInWatchList(Stock stock)
    {
        for (Stock s : mWatchList){
            if (stock.equals(s))
                return true;
        }
        return false;
    }

    public static boolean addToWatchList(Stock stock)
    {
        if (getWatchListIndex(stock) != -1)
            return false;

        ++mWatchListGeneration;
        mWatchList.add(stock);
        saveWatchList();
        return true;
    }

    public static List<Stock> getWatchList()
    {
        return Collections.unmodifiableList(mWatchList);
    }

    private static void saveWatchList()
    {
        SharedPreferences.Editor sp = mSharedPreferences.edit();
        sp.putInt("GENERATION", mWatchListGeneration);
        Stock.saveTo(sp, mWatchList);
        sp.commit();
    }

    private static DateTime getDayEndOfClose(DateTime time)
    {
        return time.withHourOfDay(16).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
    }

}

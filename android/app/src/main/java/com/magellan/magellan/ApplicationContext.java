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
    private static DateTime mStartOfTradingDay = null;
    private static DateTime mEndOfTradingDay = null;
    private static IQuoteService mQuoteService = new AlphaVantageService();

    public static IQuoteService getQuoteService()
    {
        return mQuoteService;
    }

    public static enum StockMarketHolidays2017
    {
        NewYearsDay(0, 2), // actually 0,1 but observed 0, 2
        MartinLutherKingDay(0, 16),
        WashingtonsBirthday(1, 20),
        GoodFriday(3, 14),
        MemorialDay(4, 29),
        IndependenceDay(6,4),
        LaborDay(8,4),
        ThanskgivingDay(10, 23),
        Christmas(11, 25);

        private final Date date;
        StockMarketHolidays2017(int month, int day)
        {
            date = new GregorianCalendar(2017, month, day).getTime();
        }
    }

    public static enum StockMarketHolidays2018
    {
        NewYearsDay(0, 1),
        MartinLutherKingDay(0, 15),
        WashingtonsBirthday(1, 19),
        GoodFriday(3, 30),
        MemorialDay(4, 28),
        IndependenceDay(6,4),
        LaborDay(8,3),
        ThanskgivingDay(10, 22),
        Christmas(11, 25);

        public final Date date;
        StockMarketHolidays2018(int month, int day)
        {
            date = new GregorianCalendar(2018, month, day).getTime();
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

    public static DateTime getLastTradingCloseTimeBefore(DateTime dateTime)
    {
        DateTime dateTimeCopy = new DateTime(dateTime);
        boolean checkIfTradingDay = true;
        while (checkIfTradingDay)
        {
            checkIfTradingDay = false;
            int year = dateTimeCopy.year().get();

            Calendar cal = Calendar.getInstance();
            cal.setTime(dateTimeCopy.toDate());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date currentTradingDate = cal.getTime();
            if (year == 2017)
            {
                for (StockMarketHolidays2017 holiday : StockMarketHolidays2017.values())
                {
                    if (holiday.date.compareTo(currentTradingDate) == 0)
                    {
                        dateTimeCopy =  dateTimeCopy.minusDays(1);
                        checkIfTradingDay = true;
                        break;
                    }
                }
            }
            else if (year == 2018)
            {
                for (StockMarketHolidays2018 holiday : StockMarketHolidays2018.values())
                {
                    if (holiday.date.compareTo(currentTradingDate) == 0)
                    {
                        dateTimeCopy =  dateTimeCopy.minusDays(1);
                        checkIfTradingDay = true;
                        break;
                    }
                }
            }
            else
                Log.e("Magellan", "ERROR: Holiday / Blacklist days not handed for year " + Integer.toString(year));

            int hourOfDay = dateTimeCopy.hourOfDay().get();
            int minuteOfDay = dateTimeCopy.minuteOfDay().get();

            int endDay = dateTimeCopy.dayOfWeek().get();
            if (endDay == 6)
                {
                dateTimeCopy = dateTimeCopy.minus(Duration.standardDays(1));
                checkIfTradingDay = true;
            }
            else if (endDay == 7)
            {
                dateTimeCopy = dateTimeCopy.minus(Duration.standardDays(2));
                checkIfTradingDay = true;
            }
            else if (hourOfDay < 9 || ( hourOfDay == 9  && minuteOfDay < 40)) // make sure we have at least 2 quotes (as line graphs will not work with one entry point)
            {
                checkIfTradingDay = true;

                if (endDay == 1)
                    dateTimeCopy = dateTimeCopy.minus(Duration.standardDays(3));
                else
                    dateTimeCopy = dateTimeCopy.minus(Duration.standardDays(1));
            }
        }

        dateTimeCopy = dateTimeCopy.withHourOfDay(16).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0); // 4:00 pm is NYSE close

        return dateTimeCopy;
    }

    public static DateTime getLastTradingDayCloseTime()
    {
        return getLastTradingCloseTimeBefore(DateTime.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"))));

    }

    public static DateTime getOpenTimeForCloseTime(DateTime closeTime)
    {
        return closeTime.minusHours(7).plusMinutes(30); // 9:30 EST is NYSE open*/
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

    public static boolean addToWatchList(Stock stock)
    {
        for (Stock s : mWatchList){
            if (stock.equals(s))
                return false;
        }

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
}

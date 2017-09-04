package com.magellan.magellan;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.magellan.magellan.quote.IQuoteService;
import com.magellan.magellan.service.alphavantage.AlphaVantageService;
import com.magellan.magellan.stock.Stock;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
        if (mEndOfTradingDay != null)
            return mEndOfTradingDay;

        DateTime now = DateTime.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST")));
        int hourOfDay = now.hourOfDay().get();
        int minuteOfDay = now.minuteOfDay().get();

        mEndOfTradingDay =  now.withHourOfDay(16).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0); // 4:00 pm is NYSE close

        int endDay = mEndOfTradingDay.dayOfWeek().get();
        if (endDay == 6)
            mEndOfTradingDay = mEndOfTradingDay.minus(Duration.standardDays(1));
        else if (endDay == 7)
            mEndOfTradingDay = mEndOfTradingDay.minus(Duration.standardDays(2));
        else if (hourOfDay < 9 || ( hourOfDay == 9  && minuteOfDay < 40)) // make sure we have at least 2 quotes
        {
            if (endDay == 1)
                mEndOfTradingDay = mEndOfTradingDay.minus(Duration.standardDays(3));
            else
                mEndOfTradingDay = mEndOfTradingDay.minus(Duration.standardDays(1));
        }

        return mEndOfTradingDay;
    }

    public static DateTime getLastTradingDayOpenTime()
    {
        if (mStartOfTradingDay != null)
            return mStartOfTradingDay;

        mStartOfTradingDay = getLastTradingDayCloseTime().minusHours(7).plusMinutes(30); // 9:30 EST is NYSE open*/
        return mStartOfTradingDay;
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

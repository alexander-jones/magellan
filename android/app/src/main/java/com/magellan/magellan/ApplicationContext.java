package com.magellan.magellan;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.magellan.magellan.equity.Equity;
import com.magellan.magellan.quote.IQuoteService;
import com.magellan.magellan.service.alphavantage.AlphaVantageService;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.chrono.GregorianChronology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplicationContext {

    private static String PREFERENCE_KEY = "APPLICATION_CONTEXT";

    private static List<Equity> mSectorIndices = new ArrayList<Equity>();

    private static Context mContext;
    private static int mWatchListGeneration = 0;
    private static List<Equity> mWatchList = null;
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

    public static List<Equity> getSectorIndices()
    {
        if (mSectorIndices.size() == 0) {
            mSectorIndices.add(new Equity("ABAQ", "ABA Community Bank NASDAQ Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("BIXX", "BetterInvesting 100 Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("BIXR", "BetterInvesting 100 Total Return Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("BXN", "CBOE NASDAQ-100 BuyWrite Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("INDU", "Dow Industrials", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("TRAN", "Dow Transportation", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("UTIL", "Dow15 Utilities" , "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("FTSELC", "FTSE NASDAQ Large Cap Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("FTSEMC", "FTSE NASDAQ Mid Cap Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("FTSESC", "FTSE NASDAQ Small Cap Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("BKX", "KBW Bank Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("MFX", "KBW Mortgage Finance Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("KRX", "KBW Regional Banking Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("IXBK", "NASDAQ Bank", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("NBI", "NASDAQ Biotechnology", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("NBIE", "NASDAQ Biotechnology Equal Weighted Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("CHXN", "NASDAQ China Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("CELS", "NASDAQ Clean Edge Green Energy Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("CEXX", "NASDAQ Clean Edge Green Energy Total Return Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("NQCICLER", "NASDAQ Commodity Crude Oil Index ER", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("NQCIGCER", "ABA Community Bank NASDAQ Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("NQCIHGER", "NASDAQ Commodity Gold Index ER", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("NQCINGER", "NASDAQ Commodity Natural Gas Index ER", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("NQCISIER", "NASDAQ Commodity Silver Index ER", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("IXCO", "NASDAQ Computer", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("DTEC", "NASDAQ Dallas Regional Chamber Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("DIVQ", "NASDAQ Dividend Achievers Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("DVQT", "NASDAQ Dividend Achievers Total Return Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("IXFIN", "NASDAQ Financial-100", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("IXHC", "NASDAQ Health Care Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("IXID", "NASDAQ Industrial", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("IXIS", "NASDAQ Insurance", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("QNET", "NASDAQ Internet Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("ISRQ", "NASDAQ Israel", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("ISRX", "NASDAQ Israel Total Return", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("QWND", "NASDAQ OMX Clean Edge Global Wind Energy Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("QAGR", "NASDAQ OMX Global Agriculture Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("QCOL", "NASDAQ OMX Global Coal Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("QGLD", "NASDAQ OMX Global Gold & Precious Metals Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("QSTL", "NASDAQ OMX Global Steel Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("QGRI", "NASDAQ OMX Government Relief Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("IXFN", "NASDAQ Other Finance", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("NXTQ", "NASDAQ Q-50 Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("IXTC", "NASDAQ Telecommunications", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("IXTR", "NASDAQ Transportation", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("NDXX", "NASDAQ-100 Ex-Tech Sector Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("NDXT", "NASDAQ-100 Technology Sector Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("XCM", "PHLX Chemicals Sector", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("DFX", "PHLX Defense Sector", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("RXS", "PHLX Drug Sector", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("XEX", "PHLX Europe Sector", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("XAU", "PHLX Gold/Silver Sector", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("HGX", "PHLX Housing Sector", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("SHX", "PHLX Marine Shipping Sector", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("MXZ", "PHLX Medical Device Sector", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("OSX", "PHLX Oil Service Sector", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("XRE", "PHLX Retail Sector", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("SOX", "PHLX Semiconductor Sector", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("SXP", "PHLX Sports Sector", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("UTY", "PHLX Utility Sector", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("RUI/E", "Russell 1000 Growth", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("RUI/P", "Russell 1000 Value", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("RUTE2KG", "Russell 2000 Growth", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("RUTP2KV", "Russell 2000 Value", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("SVO", "SIG Energy MLP Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("EPX", "SIG Oil Exploration & Production Index", "NASDAQ", "Index"));
            mSectorIndices.add(new Equity("HAUL", "Wilder NASDAQ OMX Global Energy Efficient Transport Index", "NASDAQ", "Index"));
        }
        return mSectorIndices;
    }

    // initialize default settins for any charts in this activity
    public static void initializeSimpleChart(Context context, CombinedChart chart)
    {
        float internal_spacing = context.getResources().getDimension(R.dimen.spacing_internal);
        chart.setNoDataText("");
        chart.setTouchEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawMarkers(false);
        chart.setDrawBorders(false);
        chart.getDescription().setText("");
        chart.getLegend().setEnabled(false);
        chart.setViewPortOffsets(internal_spacing, internal_spacing, internal_spacing, internal_spacing);

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

    public static DateTime getLastTradingDayFromTime(DateTime inTime)
    {
        DateTime ret = inTime.minusDays(1);
        boolean isTradingDay = false;
        while (!isTradingDay)
        {
            isTradingDay = true;
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

            if (!isTradingDay)
                ret = ret.minusDays(1);
        }
        return ret;
    }

    public static void init(Context context)
    {
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        mWatchList = Equity.loadFrom(mSharedPreferences);
        mWatchListGeneration = mSharedPreferences.getInt("GENERATION", 0);
        if (mWatchList == null){
            mWatchList = new ArrayList<Equity>();
        }
    }

    public static int getWatchListGeneration() { return mWatchListGeneration;}

    public static int getWatchListIndex(Equity equity)
    {
        int index = -1;
        for (int i =0; i < mWatchList.size(); ++i){
            if (equity.equals(mWatchList.get(i)))
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

    public static boolean addToWatchList(Equity equity)
    {
        if (getWatchListIndex(equity) != -1)
            return false;

        ++mWatchListGeneration;
        mWatchList.add(equity);
        saveWatchList();
        return true;
    }

    public static List<Equity> getWatchList()
    {
        return Collections.unmodifiableList(mWatchList);
    }

    private static void saveWatchList()
    {
        SharedPreferences.Editor sp = mSharedPreferences.edit();
        sp.putInt("GENERATION", mWatchListGeneration);
        Equity.saveTo(sp, mWatchList);
        sp.commit();
    }

    private static DateTime getDayEndOfClose(DateTime time)
    {
        return time.withHourOfDay(16).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
    }

}

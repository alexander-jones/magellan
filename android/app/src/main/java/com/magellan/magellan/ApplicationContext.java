package com.magellan.magellan;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.magellan.magellan.equity.Equity;
import com.magellan.magellan.quote.IQuoteService;
import com.magellan.magellan.service.alphavantage.AlphaVantageService;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.chrono.GregorianChronology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplicationContext {

    private static String PREFERENCE_KEY = "APPLICATION_CONTEXT";
    private static String WATCHLIST_PREFIX = "WATCHLIST";
    private static String COMPARISON_PREFIX = "COMPARISON";

    private static List<Equity> mMajorIndices = null;
    private static List<Equity> mSectorIndices = null;

    private static Context mContext;
    private static int mWatchListGeneration = 0;
    private static List<Equity> mWatchList = null;

    private static int mComparisonEquityGeneration = 0;
    private static List<Equity> mComparisonEquities = null;
    private static List<Integer> mComparisonEquityColors = null;

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


    public static void init(Context context)
    {
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        mWatchList = Equity.loadFrom(mSharedPreferences, WATCHLIST_PREFIX);
        mWatchListGeneration = mSharedPreferences.getInt(WATCHLIST_PREFIX + "_GENERATION", 0);

        mComparisonEquityGeneration = mSharedPreferences.getInt(COMPARISON_PREFIX + "_GENERATION", 0);
        mComparisonEquityColors = null;
        mComparisonEquities = Equity.loadFrom(mSharedPreferences, COMPARISON_PREFIX);

        int numColors = mSharedPreferences.getInt(COMPARISON_PREFIX + "_COLOR_COUNT", -1);
        if (numColors != -1)
            mComparisonEquityColors = new ArrayList<Integer>(numColors);

        for (int i = 0; i < numColors; i++)
            mComparisonEquityColors.add(mSharedPreferences.getInt(COMPARISON_PREFIX + "_COLOR_" + Integer.toString(i), 0));

        if (mComparisonEquities == null)
        {
            if (BuildConfig.DEBUG) {
                Assert.assertEquals(mComparisonEquityColors, null);
            }

            // if no comparison equities have been saved yet show the S&P 500, Dow Jones, and NASDAQ Composite
            mComparisonEquities = new ArrayList<Equity>();
            List<Equity> majEquities = getMajorIndices();
            mComparisonEquities.add(majEquities.get(0));
            mComparisonEquities.add(majEquities.get(1));
            mComparisonEquities.add(majEquities.get(2));

            mComparisonEquityColors = new ArrayList<Integer>();
            mComparisonEquityColors.add(Color.parseColor("#4a81ff"));
            mComparisonEquityColors.add(Color.parseColor("#ff8d28"));
            mComparisonEquityColors.add(Color.parseColor("#d428ff"));
        }

        if (BuildConfig.DEBUG) {
            if (mComparisonEquities != null)
                Assert.assertNotSame(mComparisonEquityColors, null);
        }

        if (mWatchList == null)
        {
            mWatchList = new ArrayList<Equity>();
            // TODO: set default equities if this is first boot
        }
    }

    public static List<Equity> getMajorIndices()
    {
        if (mMajorIndices == null) {
            mMajorIndices = new ArrayList<Equity>();
            mMajorIndices.add(new Equity("SPX", "S&P 500", "N/A", "Index"));
            mMajorIndices.add(new Equity("IXIC", "NASDAQ Composite", "N/A", "Index"));
            mMajorIndices.add(new Equity("DJIA", "Dow Jones Industrial Average", "N/A", "Index"));
            mMajorIndices.add(new Equity("XAX", "Amex Composite", "N/A", "Index"));
            mMajorIndices.add(new Equity("VOLNDX", "DWS NASDAQ-100 Volatility Target Index", "N/A", "Index"));
            mMajorIndices.add(new Equity("FTSEQ500", "FTSE NASDAQ 500 Index", "N/A", "Index"));
            mMajorIndices.add(new Equity("RCMP", "NASDAQ Capital Market Composite Index", "N/A", "Index"));
            mMajorIndices.add(new Equity("NQGM", "NASDAQ Global Market Composite", "N/A", "Index"));
            mMajorIndices.add(new Equity("NQGS", "NASDAQ Global Select Market Composite", "N/A", "Index"));
            mMajorIndices.add(new Equity("QOMX", "NASDAQ OMX 100 Index", "N/A", "Index"));
            mMajorIndices.add(new Equity("LTI", "NASDAQ OMX AeA Illinois Tech Index", "N/A", "Index"));
            mMajorIndices.add(new Equity("QMEA", "NASDAQ OMX Middle East North Africa Index", "N/A", "Index"));
            mMajorIndices.add(new Equity("IXNDX", "NASDAQ-100", "N/A", "Index"));
            mMajorIndices.add(new Equity("NYA", "NYSE Composite", "N/A", "Index"));
            mMajorIndices.add(new Equity("OMXB10", "OMX Baltic 10", "N/A", "Index"));
            mMajorIndices.add(new Equity("OMXC20", "OMX Copenhagen 20", "N/A", "Index"));
            mMajorIndices.add(new Equity("OMXH25", "OMX Helsinki 25", "N/A", "Index"));
            mMajorIndices.add(new Equity("OMXN40", "OMX Nordic 40", "N/A", "Index"));
            mMajorIndices.add(new Equity("OMXS30", "OMX Stockholm 30 Index", "N/A", "Index"));
            mMajorIndices.add(new Equity("RUI", "Russell 1000", "N/A", "Index"));
            mMajorIndices.add(new Equity("RUT", "Russell 2000", "N/A", "Index"));
            mMajorIndices.add(new Equity("RUA", "Russell 3000", "N/A", "Index"));
            mMajorIndices.add(new Equity("OEX", "S&P 100", "N/A", "Index"));
            mMajorIndices.add(new Equity("MID", "S&P MidCap", "N/A", "Index"));
            mMajorIndices.add(new Equity("NDXE", "The NASDAQ-100 Equal Weighted Index", "N/A", "Index"));
            mMajorIndices.add(new Equity("VINX30", "VINX 30", "N/A", "Index"));
            mMajorIndices.add(new Equity("WLX", "Wilshire 5000", "N/A", "Index"));
        }
        return Collections.unmodifiableList(mMajorIndices);
    }

    public static List<Equity> getSectorIndices()
    {
        if (mSectorIndices == null) {
            mSectorIndices = new ArrayList<Equity>();
            mSectorIndices.add(new Equity("ABAQ", "ABA Community Bank NASDAQ Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("BIXX", "BetterInvesting 100 Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("BIXR", "BetterInvesting 100 Total Return Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("BXN", "CBOE NASDAQ-100 BuyWrite Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("INDU", "Dow Industrials", "N/A", "Index"));
            mSectorIndices.add(new Equity("TRAN", "Dow Transportation", "N/A", "Index"));
            mSectorIndices.add(new Equity("UTIL", "Dow15 Utilities" , "N/A", "Index"));
            mSectorIndices.add(new Equity("FTSELC", "FTSE NASDAQ Large Cap Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("FTSEMC", "FTSE NASDAQ Mid Cap Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("FTSESC", "FTSE NASDAQ Small Cap Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("BKX", "KBW Bank Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("MFX", "KBW Mortgage Finance Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("KRX", "KBW Regional Banking Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("IXBK", "NASDAQ Bank", "N/A", "Index"));
            mSectorIndices.add(new Equity("NBI", "NASDAQ Biotechnology", "N/A", "Index"));
            mSectorIndices.add(new Equity("NBIE", "NASDAQ Biotechnology Equal Weighted Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("CHXN", "NASDAQ China Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("CELS", "NASDAQ Clean Edge Green Energy Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("CEXX", "NASDAQ Clean Edge Green Energy Total Return Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("NQCICLER", "NASDAQ Commodity Crude Oil Index ER", "N/A", "Index"));
            mSectorIndices.add(new Equity("NQCIGCER", "ABA Community Bank NASDAQ Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("NQCIHGER", "NASDAQ Commodity Gold Index ER", "N/A", "Index"));
            mSectorIndices.add(new Equity("NQCINGER", "NASDAQ Commodity Natural Gas Index ER", "N/A", "Index"));
            mSectorIndices.add(new Equity("NQCISIER", "NASDAQ Commodity Silver Index ER", "N/A", "Index"));
            mSectorIndices.add(new Equity("IXCO", "NASDAQ Computer", "N/A", "Index"));
            mSectorIndices.add(new Equity("DTEC", "NASDAQ Dallas Regional Chamber Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("DIVQ", "NASDAQ Dividend Achievers Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("DVQT", "NASDAQ Dividend Achievers Total Return Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("IXFIN", "NASDAQ Financial-100", "N/A", "Index"));
            mSectorIndices.add(new Equity("IXHC", "NASDAQ Health Care Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("IXID", "NASDAQ Industrial", "N/A", "Index"));
            mSectorIndices.add(new Equity("IXIS", "NASDAQ Insurance", "N/A", "Index"));
            mSectorIndices.add(new Equity("QNET", "NASDAQ Internet Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("ISRQ", "NASDAQ Israel", "N/A", "Index"));
            mSectorIndices.add(new Equity("ISRX", "NASDAQ Israel Total Return", "N/A", "Index"));
            mSectorIndices.add(new Equity("QWND", "NASDAQ OMX Clean Edge Global Wind Energy Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("QAGR", "NASDAQ OMX Global Agriculture Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("QCOL", "NASDAQ OMX Global Coal Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("QGLD", "NASDAQ OMX Global Gold & Precious Metals Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("QSTL", "NASDAQ OMX Global Steel Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("QGRI", "NASDAQ OMX Government Relief Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("IXFN", "NASDAQ Other Finance", "N/A", "Index"));
            mSectorIndices.add(new Equity("NXTQ", "NASDAQ Q-50 Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("IXTC", "NASDAQ Telecommunications", "N/A", "Index"));
            mSectorIndices.add(new Equity("IXTR", "NASDAQ Transportation", "N/A", "Index"));
            mSectorIndices.add(new Equity("NDXX", "NASDAQ-100 Ex-Tech Sector Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("NDXT", "NASDAQ-100 Technology Sector Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("XCM", "PHLX Chemicals Sector", "N/A", "Index"));
            mSectorIndices.add(new Equity("DFX", "PHLX Defense Sector", "N/A", "Index"));
            mSectorIndices.add(new Equity("RXS", "PHLX Drug Sector", "N/A", "Index"));
            mSectorIndices.add(new Equity("XEX", "PHLX Europe Sector", "N/A", "Index"));
            mSectorIndices.add(new Equity("XAU", "PHLX Gold/Silver Sector", "N/A", "Index"));
            mSectorIndices.add(new Equity("HGX", "PHLX Housing Sector", "N/A", "Index"));
            mSectorIndices.add(new Equity("SHX", "PHLX Marine Shipping Sector", "N/A", "Index"));
            mSectorIndices.add(new Equity("MXZ", "PHLX Medical Device Sector", "N/A", "Index"));
            mSectorIndices.add(new Equity("OSX", "PHLX Oil Service Sector", "N/A", "Index"));
            mSectorIndices.add(new Equity("XRE", "PHLX Retail Sector", "N/A", "Index"));
            mSectorIndices.add(new Equity("SOX", "PHLX Semiconductor Sector", "N/A", "Index"));
            mSectorIndices.add(new Equity("SXP", "PHLX Sports Sector", "N/A", "Index"));
            mSectorIndices.add(new Equity("UTY", "PHLX Utility Sector", "N/A", "Index"));
            mSectorIndices.add(new Equity("RUI/E", "Russell 1000 Growth", "N/A", "Index"));
            mSectorIndices.add(new Equity("RUI/P", "Russell 1000 Value", "N/A", "Index"));
            mSectorIndices.add(new Equity("RUTE2KG", "Russell 2000 Growth", "N/A", "Index"));
            mSectorIndices.add(new Equity("RUTP2KV", "Russell 2000 Value", "N/A", "Index"));
            mSectorIndices.add(new Equity("SVO", "SIG Energy MLP Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("EPX", "SIG Oil Exploration & Production Index", "N/A", "Index"));
            mSectorIndices.add(new Equity("HAUL", "Wilder NASDAQ OMX Global Energy Efficient Transport Index", "N/A", "Index"));
        }
        return Collections.unmodifiableList(mSectorIndices);
    }

    public static int getComparisonEquityGeneration()
    {
        return mComparisonEquityGeneration;
    }

    public static boolean addToComparisonEquities(Equity equity, int color)
    {
        if (getComparisonEquityIndex(equity) != -1)
            return false;

        ++mComparisonEquityGeneration;
        mComparisonEquities.add(equity);
        mComparisonEquityColors.add(color);
        saveComparisonEquities();
        return true;
    }

    public static boolean removeFromComparisonEquities(int index)
    {
        if (index < 0 || index >= mComparisonEquities.size())
            return false;

        ++mComparisonEquityGeneration;
        mComparisonEquities.remove(index);
        mComparisonEquityColors.remove(index);
        saveComparisonEquities();
        return true;
    }

    public static List<Integer> getComparisonEquityColors()
    {
        return Collections.unmodifiableList(mComparisonEquityColors);
    }

    public static int getComparisonEquityIndex(Equity equity) {
        int index = -1;
        for (int i =0; i < mComparisonEquities.size(); ++i){
            if (equity.equals(mComparisonEquities.get(i)))
            {
                index = i;
                break;
            }
        }
        return index;
    }

    public static List<Equity> getComparisonEquities()
    {
        return Collections.unmodifiableList(mComparisonEquities);
    }

    private static void saveComparisonEquities()
    {
        SharedPreferences.Editor sp = mSharedPreferences.edit();
        sp.putInt(COMPARISON_PREFIX + "_GENERATION", mComparisonEquityGeneration);
        Equity.saveTo(sp, mComparisonEquities, COMPARISON_PREFIX);

        sp.putInt(COMPARISON_PREFIX + "_COLOR_COUNT", mComparisonEquityColors.size());
        for (int i = 0; i < mComparisonEquityColors.size(); i++)
            sp.putInt(COMPARISON_PREFIX + "_COLOR_" + Integer.toString(i), mComparisonEquityColors.get(i));

        sp.commit();
    }

    public static int getWatchListGeneration()
    {
        return mWatchListGeneration;
    }

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
        sp.putInt(WATCHLIST_PREFIX + "_GENERATION", mWatchListGeneration);
        Equity.saveTo(sp, mWatchList, WATCHLIST_PREFIX);
        sp.commit();
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

    public static DateTime getCloseTimeWithSameDate(DateTime time)
    {
        return time.withHourOfDay(16).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0); // 4:00 pm is NYSE close
    }

    public static DateTime getOpenTimeForCloseTime(DateTime closeTime)
    {
        return closeTime.minusHours(7).plusMinutes(30); // 9:30 EST is NYSE open*/
    }

    public static DateTime getLastCloseDateTime()
    {
        DateTime now = DateTime.now(getTradingTimeZone());
        return getLastTradingDateTimeFrom(now);
    }

    public static DateTime getLastTradingDateTimeFrom(DateTime inTime)
    {
        DateTime ret = inTime;
        int hourOfDay = ret.hourOfDay().get();
        int minuteOfHour = ret.minuteOfHour().get();
        if (hourOfDay > 16)
            ret = getCloseTimeWithSameDate(ret);
        else if (hourOfDay < 9 || (hourOfDay == 9 && minuteOfHour < 30))
            ret = getCloseTimeWithSameDate(ret.minusDays(1));

        do
        {
            int daysToUnwind = 0;
            int endDay = ret.dayOfWeek().get();
            if (endDay == 6) // Saturday?
                daysToUnwind = 1;
            else if (endDay == 7) // Sunday?
                daysToUnwind = 2;
            else
            {
                DateTime currentTradingDate = ret.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                int year = ret.year().get();
                if (year == 2017) {
                    for (StockMarketHolidays2017 holiday : StockMarketHolidays2017.values()) {
                        if (holiday.date.isEqual(currentTradingDate)) {
                            daysToUnwind = 1;
                            break;
                        }
                    }
                } else if (year == 2018) {
                    for (StockMarketHolidays2018 holiday : StockMarketHolidays2018.values()) {
                        if (holiday.date.isEqual(currentTradingDate)) {
                            daysToUnwind = 1;
                            break;
                        }
                    }
                } else if (year == 2019) {
                    for (StockMarketHolidays2019 holiday : StockMarketHolidays2019.values()) {
                        if (holiday.date.isEqual(currentTradingDate)) {
                            daysToUnwind = 1;
                            break;
                        }
                    }
                } else {
                    Log.e("Magellan", "ERROR: Holiday / Blacklist days not handed for year " + Integer.toString(year));
                    break;
                }
            }

            if (daysToUnwind == 0)
                break;
            else
                ret = getCloseTimeWithSameDate(ret.minusDays(daysToUnwind));
        }
        while (true);

        return ret;
    }

}

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
import com.magellan.magellan.service.barchart.BarChartService;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.chrono.GregorianChronology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplicationContext {
    private static List<Equity> mMajorIndices = null;
    private static List<Equity> mSectorIndices = null;
    private static IQuoteService mQuoteService = new AlphaVantageService();

    public static IQuoteService getQuoteService()
    {
        return mQuoteService;
    }

    public static void init()
    {

        /*
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
        */;
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

    // initialize default settings for any charts in this activity
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

}

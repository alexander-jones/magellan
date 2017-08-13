package com.magellan.magellan;

import java.util.List;

import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.util.TypedValue;
import android.util.Log;
import android.os.Bundle;
import android.view.MotionEvent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.TimeZone;

public class StockAnalyzerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnChartGestureListener, OnChartValueSelectedListener, Stock.HistoryQueryListener{

    private enum QuotePeriod
    {
        OneDay,
        OneWeek,
        OneMonth,
        ThreeMonths,
        OneYear,
        FiveYears,
        TenYears
    }

    private static int CHART_MARGIN = 10;
    private static int CHART_SPACING = 5;

    private String mSymbol;

    private TextView mPriceText;
    private TextView mPriceChangeText;
    private TextView mVolumeText;
    private TextView mVolumeChangeText;
    private TextView mStockText;
    private TextView mDateText;
    private RecyclerView mPriceLayersContainer;
    private Metric.LayerAdapter mPriceLayerAdapter;
    private RecyclerView mVolumeLayersContainer;
    private Metric.LayerAdapter mVolumeLayerAdapter;
    private TabLayout mIntervalTabLayout;

    private CombinedChart mPriceChart;
    private CombinedData mPriceChartData;

    private CombinedChart mVolumeChart;
    private CombinedData mVolumeChartData;

    private Stock.HistoryQueryTask mQuoteTask;
    private QueryContext mLastQuery = new QueryContext();

    private List<Metric.IChartLayer> mStockPriceLayers = new ArrayList<Metric.IChartLayer>();
    private List<Metric.IChartLayer> mVolumeLayers = new ArrayList<Metric.IChartLayer>();

    private class QueryContext
    {
        Stock.HistoryQuery query;
        Stock.IQuoteCollection results;
        QuotePeriod quotePeriod;
        boolean complete = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_analyzer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mStockText = (TextView) findViewById(R.id.stock);
        mDateText = (TextView) findViewById(R.id.date);
        mPriceText = (TextView) findViewById(R.id.price);
        mPriceChangeText = (TextView) findViewById(R.id.price_change);
        mVolumeText = (TextView) findViewById(R.id.volume);
        mVolumeChangeText = (TextView) findViewById(R.id.volume_change);
        mIntervalTabLayout = (TabLayout) findViewById(R.id.interval_tabs);

        mPriceLayersContainer = (RecyclerView) findViewById(R.id.price_layers);
        mPriceLayersContainer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mPriceLayersContainer.setHasFixedSize(true);

        List<String> testActiveLayerLabels = new ArrayList<String>();
        testActiveLayerLabels.add("SP");
        mPriceLayerAdapter = new Metric.LayerAdapter(testActiveLayerLabels, ContextCompat.getColor(this, R.color.colorSecondary), Color.WHITE);
        mPriceLayersContainer.setAdapter(mPriceLayerAdapter);

        mVolumeLayersContainer = (RecyclerView) findViewById(R.id.volume_layers);
        mVolumeLayersContainer = (RecyclerView) findViewById(R.id.volume_layers);
        mVolumeLayersContainer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mVolumeLayersContainer.setHasFixedSize(true);

        List<String> testActiveVolumeLayerLabels = new ArrayList<String>();
        testActiveVolumeLayerLabels.add("Vol");
        mVolumeLayerAdapter = new Metric.LayerAdapter(testActiveVolumeLayerLabels, ContextCompat.getColor(this, R.color.colorSecondary), Color.WHITE);
        mVolumeLayersContainer.setAdapter(mVolumeLayerAdapter);

        float chart_margin_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CHART_MARGIN, getResources().getDisplayMetrics());
        float chart_spacing_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CHART_SPACING, getResources().getDisplayMetrics());

        mPriceChart = (CombinedChart) findViewById(R.id.price_chart);
        mPriceChartData = new CombinedData();
        initializeChart(mPriceChart);
        mPriceChart.setViewPortOffsets(chart_margin_px, chart_margin_px, chart_margin_px, 0);

        mVolumeChart = (CombinedChart) findViewById(R.id.volume_chart);
        mVolumeChartData = new CombinedData();
        initializeChart(mVolumeChart);
        mVolumeChart.setViewPortOffsets(chart_margin_px,chart_margin_px,chart_margin_px,chart_spacing_px);

        mStockPriceLayers.add(new StockPriceMetric.BasicChartLayer(StockPriceMetric.ChartType.Line));
        mVolumeLayers.add(new VolumeMetric.BasicChartLayer());

        for (Metric.IChartLayer layer : mStockPriceLayers)
            layer.init(this, mPriceChartData);

        for (Metric.IChartLayer layer : mVolumeLayers)
            layer.init(this, mVolumeChartData);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSymbol = "AMC";
        mStockText.setText(mSymbol);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        launchTaskForTab(mIntervalTabLayout.getSelectedTabPosition());
        mIntervalTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                launchTaskForTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    
    // initialize default settins for any charts in this activity
    private void initializeChart(CombinedChart chart)
    {
        chart.setNoDataText("");
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(true);
        chart.setOnChartGestureListener(this);
        chart.setOnChartValueSelectedListener(this);
        chart.setDrawGridBackground(false);
        chart.setDrawMarkers(false);
        chart.setDrawBorders(true);
        chart.getDescription().setText("");
        chart.disableScroll();
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setScaleEnabled(false);
        chart.getLegend().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawLabels(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis =  chart.getAxisRight();
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawGridLines(false);
    }

    private void launchTaskForTab(int position)
    {
        DateTime start = null;
        Stock.IntervalUnit intervalUnit = null;
        int interval = 1;
        DateTime end = DateTime.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST")))
                .withHourOfDay(16).withMinuteOfHour(0); // 4:00 pm is NYSE close
        int endDay = end.dayOfWeek().get();
        if (endDay == 6)
            end = end.minus(Duration.standardDays(1));
        else if (endDay == 7)
            end = end.minus(Duration.standardDays(2));
        start = end.minusHours(7).plusMinutes(30); // 9:30 EST is NYSE open

        QuotePeriod [] quotePeriods = QuotePeriod.values();
        if (position <0 || position > quotePeriods.length)
            Log.e("Magellan", "Encountered Unknown Duration Tab Index");

        mLastQuery.complete = false;
        mLastQuery.quotePeriod = quotePeriods[position];
        switch (mLastQuery.quotePeriod)
        {
            case OneDay:
                interval = 5;
                intervalUnit = Stock.IntervalUnit.Minute;
                break;
            case OneWeek:
                interval = 10;
                intervalUnit = Stock.IntervalUnit.Minute;
                start = start.minusWeeks(1);
                break;
            case OneMonth:
                intervalUnit = Stock.IntervalUnit.Day;
                start = start.minusMonths(1);
                break;
            case ThreeMonths:
                intervalUnit = Stock.IntervalUnit.Day;
                start = start.minusMonths(3);
                break;
            case OneYear:
                intervalUnit = Stock.IntervalUnit.Week;
                start = start.minusYears(1);
                break;
            case FiveYears:
                intervalUnit = Stock.IntervalUnit.Week;
                start = start.minusYears(5);
                break;
            case TenYears:
                intervalUnit = Stock.IntervalUnit.Month;
                start = start.minusYears(10);
                break;
        }
        mLastQuery.query = new Stock.HistoryQuery(mSymbol, start, end, intervalUnit, interval);
        mLastQuery.results = null;
        mQuoteTask = new Stock.HistoryQueryTask(this);
        mQuoteTask.execute(mLastQuery.query);
    }

    public void onStockHistoryRetrieved(List<Stock.IQuoteCollection> stockHistories)
    {
        boolean oneValidHistory = false;
        Duration intervalDuration = mLastQuery.query.getIntervalAsDuration();
        for (int i = 0; i < stockHistories.size(); i++)
        {
            Stock.IQuoteCollection stockHistory = stockHistories.get(i);
            if (stockHistory == null || stockHistory.size() == 0) {
                continue;
            }
            mLastQuery.results = stockHistory;

            Stock.IQuote initialQuote = stockHistory.get(0);
            Stock.IQuote finalQuote = stockHistory.get(stockHistory.size() -1);

            Duration missingStartDuration = new Duration(mLastQuery.query.start, initialQuote.getTime());
            Duration missingEndDuration = new Duration(finalQuote.getTime(), mLastQuery.query.end);

            Stock.QuoteCollectionContext quoteContext = new Stock.QuoteCollectionContext();
            quoteContext.missingStartSteps = (int)(missingStartDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());
            quoteContext.missingEndSteps = (int)(missingEndDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());

            for (Metric.IChartLayer layer : mStockPriceLayers)
                layer.onQuoteResults(stockHistory, quoteContext);

            for (Metric.IChartLayer layer : mVolumeLayers)
                layer.onQuoteResults(stockHistory, quoteContext);

            updateHeaderText(mLastQuery.results.get(0), mLastQuery.results.get(mLastQuery.results.size() -1));
            oneValidHistory = true;
        }

        mPriceChart.setData(mPriceChartData);
        mPriceChart.notifyDataSetChanged();
        mPriceChart.fitScreen();

        mVolumeChart.setData(mVolumeChartData);
        mVolumeChart.notifyDataSetChanged();
        mVolumeChart.fitScreen();
        mLastQuery.complete = oneValidHistory;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.portfolio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        if (!mLastQuery.complete)
            return;

        Entry entry = mPriceChart.getEntryByTouchPoint(me.getX(), me.getY()); // this is not the xth element but the x screen pos...
        highlightQuote(entry);
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        onNothingSelected();
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (!mLastQuery.complete)
            return;

        highlightQuote(e);
    }

    @Override
    public void onNothingSelected() {
        if (!mLastQuery.complete)
            return;

        mPriceChart.highlightValue(null);
        mVolumeChart.highlightValue(null);

        Stock.IQuote lastQuery = mLastQuery.results.get(mLastQuery.results.size() -1);
        updateHeaderText(mLastQuery.results.get(0), lastQuery);
    }

    private void highlightQuote(Entry e)
    {
        Stock.IQuote quote = (Stock.IQuote)e.getData();
        if (quote == null)
            return;

        Highlight h = new Highlight(e.getX(), 0, 0);
        h.setDataIndex(0);
        mPriceChart.highlightValue(h, false);
        mVolumeChart.highlightValue(h, false);
        updateHeaderText(mLastQuery.results.get(0), quote);
    }

    private String priceDiffToString(float priceDiff)
    {
        if (priceDiff < 0.0)
            return String.format("-$%.2f", Math.abs(priceDiff));
        else
            return String.format("+$%.2f", priceDiff);
    }

    private String volumeToString(int volume)
    {
        if (volume > 1000000000) // you never know amiright?
            return String.format("%.2fB", (float)volume / 1000000000.0f);
        else if (volume > 1000000)
            return String.format("%.2fM", (float)volume / 1000000.0f);
        else if (volume > 1000)
            return String.format("%.2fM", (float)volume / 1000.0f);
        else
            return String.format("%d", volume);
    }

    private static DateTimeFormatter timeOfDayFormatter = DateTimeFormat.forPattern("h:mm a z").withZone(DateTimeZone.getDefault());
    private static DateTimeFormatter dayOfYearFormatter = DateTimeFormat.forPattern("MMM d y");
    private void updateHeaderText(Stock.IQuote startQuote, Stock.IQuote endQuote)
    {
        float priceDiff = endQuote.getClose() - startQuote.getClose();
        float priceDiffPercent = (priceDiff / startQuote.getClose()) * 100.0f;

        int volumeDiff = endQuote.getVolume() - startQuote.getVolume();
        float volumeDiffPercent = ((float)volumeDiff / (float)startQuote.getVolume()) * 100.0f;

        DateTime endTime = endQuote.getTime();
        String timeString;
        switch (mLastQuery.quotePeriod)
        {
            case OneDay:
                timeString = timeOfDayFormatter.print(endTime);
                break;
            case OneWeek:
                timeString = timeOfDayFormatter.print(endTime);
                break;
            case OneMonth:
                timeString = dayOfYearFormatter.print(endTime);
                break;
            case ThreeMonths:
                timeString = dayOfYearFormatter.print(endTime);
                break;
            case OneYear:
                timeString = dayOfYearFormatter.print(endTime);
                break;
            case FiveYears:
                timeString = dayOfYearFormatter.print(endTime);
                break;
            case TenYears:
                timeString = dayOfYearFormatter.print(endTime);
                break;
            default:
                Log.e("Magellan", "Encountered Unknown Duration Tab Index");
                timeString = "";
                break;
        }
        mDateText.setText(timeString);
        mPriceText.setText(String.format("$%.2f", endQuote.getClose()));
        mPriceChangeText.setText(String.format("%s (%.2f%%)", priceDiffToString(priceDiff), priceDiffPercent));
        mVolumeText.setText(volumeToString(endQuote.getVolume()));
        mVolumeChangeText.setText(String.format("%s (%.2f%%)", volumeToString(volumeDiff), volumeDiffPercent));
    }

}

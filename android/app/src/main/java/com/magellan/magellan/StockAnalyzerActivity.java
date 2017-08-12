package com.magellan.magellan;

import java.util.List;

import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import android.widget.GridLayout;
import android.text.Html;

import com.barchart.ondemand.util.QueryUtil;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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

    private String mSymbol;

    private TextView mPrimaryValueTextView;
    private TextView mSecondaryValueTextView;
    private GridLayout mOverlayClassesContainer;
    private RecyclerView mActiveOverlaysContainer;
    private ActiveOverlayAdapter mActiveOverlayAdapter;
    private TabLayout mIntervalTabLayout;
    private LineChart mChart;

    private Stock.HistoryQueryTask mQuoteTask;
    private QueryContext mLastQuery = new QueryContext();

    private class QueryContext
    {
        Stock.HistoryQuery query;
        Stock.IQuoteCollection results;
        QuotePeriod quotePeriod;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_analyzer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mPrimaryValueTextView = (TextView) findViewById(R.id.primary_value);
        mSecondaryValueTextView = (TextView) findViewById(R.id.secondary_value);
        mIntervalTabLayout = (TabLayout) findViewById(R.id.interval_tabs);

        mOverlayClassesContainer = (GridLayout) findViewById(R.id.overlay_classes);
        mActiveOverlaysContainer = (RecyclerView) findViewById(R.id.active_overlays);
        mActiveOverlaysContainer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mActiveOverlaysContainer.setHasFixedSize(true);

        mActiveOverlayAdapter = new ActiveOverlayAdapter();
        mActiveOverlaysContainer.setAdapter(mActiveOverlayAdapter);

        mChart = (LineChart) findViewById(R.id.chart);
        mChart.setNoDataText("");
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);
        mChart.setDrawMarkers(false);
        mChart.setDrawBorders(true);
        mChart.getDescription().setText("");
        mChart.disableScroll();
        mChart.setPinchZoom(false);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setScaleEnabled(false);

        mChart.getLegend().setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawLabels(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis =  mChart.getAxisRight();
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawGridLines(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSymbol = "SNAP";

        getSupportActionBar().setTitle(mSymbol);
        launchTaskForTab(mIntervalTabLayout.getSelectedTabPosition());
        mIntervalTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                launchTaskForTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
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
        mLastQuery.query = new Stock.HistoryQuery(mSymbol, start, null, intervalUnit, interval);
        mLastQuery.results = null;
        mQuoteTask = new Stock.HistoryQueryTask(this);
        mQuoteTask.execute(mLastQuery.query);
    }

    public void onStockHistoryRetrieved(List<Stock.IQuoteCollection> stockHistories)
    {
        Duration queryDuration = new Duration(mLastQuery.query.start, mLastQuery.query.end); //.getIntervalAsDuration();
        Duration intervalDuration = mLastQuery.query.getIntervalAsDuration();
        LineData data = null;
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
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

            int missingStartSteps = (int)(missingStartDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());
            int missingEndSteps = (int)(missingEndDuration.getStandardMinutes() / queryDuration.getStandardMinutes());

            ArrayList<Entry> values = new ArrayList<Entry>();
            float startingOpen = initialQuote.getOpen();
            for (int j = 0; j < missingStartSteps; ++j)
                values.add(new Entry(j, startingOpen, null));

            for (int j = missingStartSteps; j < stockHistory.size() + missingStartSteps; ++j)
            {
                Stock.IQuote quote = stockHistory.get(j - missingStartSteps);
                values.add(new Entry(j, (float)quote.getClose(), quote));
            }

            float finalClose = finalQuote.getClose();
            for (int j = missingStartSteps +stockHistory.size() ; j < stockHistory.size() + missingStartSteps + missingEndSteps; ++j)
                values.add(new Entry(j, finalClose, null));

            /*if (mChart.getData() != null &&
                    mChart.getData().getDataSetCount() > 0) {
                LineDataSet set1 = (LineDataSet) mChart.getData().getDataSetByIndex(i);
                set1.setValues(values);
                mChart.getData().notifyDataChanged();
                continue;
            }*/

            LineDataSet set1 = new LineDataSet(values, "");
            set1.setDrawIcons(false);

            // set the line to be drawn like this "- - - - - -"
            set1.disableDashedLine();
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setHighLightColor(ContextCompat.getColor(this, R.color.colorPrimary));
            set1.setLineWidth(1f);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);
            set1.setColor(ContextCompat.getColor(this, R.color.colorSecondary));
            set1.setFillColor(ContextCompat.getColor(this, R.color.colorSecondary));
            set1.setDrawCircles(false);
            set1.setDrawValues(false);
            dataSets.add(set1);

            mPrimaryValueTextView.setText(String.format("$%.2f", values.get(values.size() - 1).getY()));
        }

        updateSecondaryTextView(mLastQuery.results.get(0), mLastQuery.results.get(mLastQuery.results.size() -1));
        data = new LineData(dataSets);
        mChart.setData(data);

        mChart.notifyDataSetChanged();
        mChart.fitScreen();
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
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

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
        mPrimaryValueTextView.setText(String.format("$%.2f", e.getY()));
        Stock.IQuote quote = (Stock.IQuote)e.getData();
        if (quote == null)
            return;

        updateSecondaryTextView(mLastQuery.results.get(0), quote);
    }

    @Override
    public void onNothingSelected() {

        updateSecondaryTextView(mLastQuery.results.get(0), mLastQuery.results.get(mLastQuery.results.size() -1));
    }

    private String priceDiffToString(float priceDiff)
    {
        if (priceDiff < 0.0)
            return String.format("-$%.2f", Math.abs(priceDiff));
        else
            return String.format("+$%.2f", priceDiff);
    }

    private static DateTimeFormatter timeOfDayFormatter = DateTimeFormat.forPattern("h:mm a z").withZone(DateTimeZone.getDefault());
    private static DateTimeFormatter dayOfYearFormatter = DateTimeFormat.forPattern("MMM d y");
    private void updateSecondaryTextView(Stock.IQuote startQuote, Stock.IQuote endQuote)
    {
        float priceDiff = (endQuote.getClose() - startQuote.getClose());
        float priceDiffPercent = (priceDiff / startQuote.getClose()) * 100.0f;

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
        mSecondaryValueTextView.setText(String.format("%s (%.2f%%) %s", priceDiffToString(priceDiff), priceDiffPercent, timeString));
    }

}

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
import org.joda.time.Duration;

import java.util.ArrayList;

public class StockAnalyzerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnChartGestureListener, OnChartValueSelectedListener, Stock.HistoryQueryListener{

    private final int ONE_DAY_TAB = 0;
    private final int ONE_WEEK_TAB = 1;
    private final int ONE_MONTH_TAB = 2;
    private final int THREE_MONTH_TAB = 3;
    private final int ONE_YEAR_TAB = 4;
    private final int FIVE_YEAR_TAB = 5;
    private final int TEN_YEAR_TAB = 6;
    private String mSymbol;

    private TextView mPrimaryValueTextView;
    private GridLayout mOverlayClassesContainer;
    private RecyclerView mActiveOverlaysContainer;
    private ActiveOverlayAdapter mActiveOverlayAdapter;
    private TabLayout mIntervalTabLayout;
    private LineChart mChart;

    private Stock.HistoryQueryTask mQuoteTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_analyzer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mPrimaryValueTextView = (TextView) findViewById(R.id.primary_value);
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

        mSymbol = "AMD";

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
        DateTime end = DateTime.now();
        int endDay = end.dayOfWeek().get();
        if (endDay == 6)
            end = end.minus(Duration.standardDays(1));
        else if (endDay == 7)
            end = end.minus(Duration.standardDays(2));

        switch (position)
        {
            case ONE_DAY_TAB:
                interval = 5;
                intervalUnit = Stock.IntervalUnit.Minute;
                start = end.minus(Duration.standardDays(1));
                break;
            case ONE_WEEK_TAB:
                interval = 30;
                intervalUnit = Stock.IntervalUnit.Minute;
                start = end.minus(Duration.standardDays(7));
                break;
            case ONE_MONTH_TAB:
                interval = 120;
                intervalUnit = Stock.IntervalUnit.Minute;
                start = end.minus(Duration.standardDays(30));
                break;
            case THREE_MONTH_TAB:
                interval = 360;
                intervalUnit = Stock.IntervalUnit.Minute;
                start = end.minus(Duration.standardDays(90));
                break;
            case ONE_YEAR_TAB:
                intervalUnit = Stock.IntervalUnit.Day;
                start = end.minus(Duration.standardDays(365));
                break;
            case FIVE_YEAR_TAB:
                intervalUnit = Stock.IntervalUnit.Week;
                start = end.minus(Duration.standardDays(5 *365));
                break;
            case TEN_YEAR_TAB:
                intervalUnit = Stock.IntervalUnit.Month;
                start = end.minus(Duration.standardDays(10 *365));
                break;
            default:
                Log.e("Magellan", "Encountered Unknown Duration Tab Index");
                break;
        }
        mQuoteTask = new Stock.HistoryQueryTask(this);
        mQuoteTask.execute(new Stock.HistoryQuery(mSymbol, start, end, intervalUnit, interval));
    }

    public void onStockHistoryRetrieved(List<Stock.IQuoteCollection> stockHistories)
    {
        LineData data = null;
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        for (int i = 0; i < stockHistories.size(); i++)
        {
            Stock.IQuoteCollection stockHistory = stockHistories.get(i);
            if (stockHistory == null) {
                continue;
            }

            ArrayList<Entry> values = new ArrayList<Entry>();
            for (int j = 0; j < stockHistory.size(); j++)
            {
                Stock.IQuote quote = stockHistory.get(j);
                values.add(new Entry(j, (float)quote.getClose(), null));
            }

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
    }

    @Override
    public void onNothingSelected() {

    }


}

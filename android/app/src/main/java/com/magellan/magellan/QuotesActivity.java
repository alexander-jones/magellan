package com.magellan.magellan;

import java.util.List;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.TypedValue;
import android.util.Log;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.magellan.magellan.metric.IMetricLayer;
import com.magellan.magellan.metric.MetricLayerButtonAdapter;
import com.magellan.magellan.metric.price.PriceCandleLayer;
import com.magellan.magellan.metric.price.PriceLineLayer;
import com.magellan.magellan.metric.price.PriceMetric;
import com.magellan.magellan.metric.volume.VolumeBarLayer;
import com.magellan.magellan.metric.volume.VolumeMetric;
import com.magellan.magellan.quote.Quote;
import com.magellan.magellan.quote.QuoteQuery;
import com.magellan.magellan.quote.IQuoteQueryListener;
import com.magellan.magellan.quote.QuoteQueryTask;
import com.magellan.magellan.service.barchart.BarChartService;
import com.magellan.magellan.stock.Stock;
import com.magellan.magellan.stock.StockQueryActivity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.TimeZone;

public class QuotesActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IQuoteQueryListener,
        ChartGestureHandler.OnHighlightListener{

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

    private int mWachListGeneration;
    private String mSymbol;
    private TextView mDateText;
    private TextView mTimeText;

    private TabLayout mStockTabLayout;
    private List<Stock> mExtraStocks = new ArrayList<Stock>();

    private TextView mPriceText;
    private TextView mPriceChangeText;
    private TextView mVolumeText;
    private TextView mVolumeChangeText;
    private RecyclerView mPriceLayersContainer;
    private MetricLayerButtonAdapter mPriceLayerAdapter;
    private RecyclerView mVolumeLayersContainer;
    private MetricLayerButtonAdapter mVolumeLayerAdapter;

    private TabLayout mIntervalTabLayout;

    private CombinedChart mPriceChart;
    private CombinedData mPriceChartData;

    private CombinedChart mVolumeChart;
    private CombinedData mVolumeChartData;


    private DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;

    private QuoteQueryTask mQuoteTask;
    private QueryContext mLastQuery = new QueryContext();

    private List<IMetricLayer> mPriceLayers = new ArrayList<IMetricLayer>();
    private List<IMetricLayer> mVolumeLayers = new ArrayList<IMetricLayer>();

    private BarChartService mQuoteService = new BarChartService();

    private class QueryContext
    {
        QuoteQuery query;
        List<Quote> results;
        QuotePeriod quotePeriod;
        boolean complete = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationContext.init(this);

        setContentView(R.layout.activity_quotes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        mDateText = (TextView) findViewById(R.id.date);
        mTimeText = (TextView) findViewById(R.id.time);
        mStockTabLayout = (TabLayout) findViewById(R.id.stock_tabs);

        View priceCard = findViewById(R.id.price_card);
        mPriceText = (TextView) priceCard.findViewById(R.id.value);
        mPriceChangeText = (TextView) priceCard.findViewById(R.id.value_change);

        mPriceLayersContainer = (RecyclerView) priceCard.findViewById(R.id.layers);
        mPriceLayersContainer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mPriceLayersContainer.setBackground(new TextDrawable(this, "Price Layers"));

        float internal_spacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        float chart_spacing_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CHART_SPACING, getResources().getDisplayMetrics());

        mPriceChart = (CombinedChart) priceCard.findViewById(R.id.chart);
        mPriceChartData = new CombinedData();
        initializeChart(mPriceChart);
        mPriceChart.getAxisRight().setValueFormatter(new PriceMetric.AxisValueFormatter());
        mPriceChart.setViewPortOffsets(0,internal_spacing,150,internal_spacing);

        mPriceLayers.add(new PriceCandleLayer(this));
        mPriceLayers.add(new PriceLineLayer(this));

        List<String> priceLayerLabels = new ArrayList<String>();
        for (IMetricLayer layer : mPriceLayers)
            priceLayerLabels.add(layer.getShortName());
        mPriceLayerAdapter = new MetricLayerButtonAdapter(priceLayerLabels);
        mPriceLayersContainer.setAdapter(mPriceLayerAdapter);

        View volumeCard = findViewById(R.id.volume_card);
        mVolumeText = (TextView) volumeCard.findViewById(R.id.value);
        mVolumeChangeText = (TextView) volumeCard.findViewById(R.id.value_change);
        mIntervalTabLayout = (TabLayout) findViewById(R.id.interval_tabs);

        mVolumeLayersContainer = (RecyclerView) volumeCard.findViewById(R.id.layers);
        mVolumeLayersContainer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mVolumeLayersContainer.setBackground(new TextDrawable(this, "Volume Layers"));

        mVolumeChart = (CombinedChart) volumeCard.findViewById(R.id.chart);
        mVolumeChartData = new CombinedData();
        initializeChart(mVolumeChart);
        mVolumeChart.getAxisRight().setValueFormatter(new VolumeMetric.AxisValueFormatter());
        mVolumeChart.setViewPortOffsets(0,internal_spacing,150 - (internal_spacing / 2.0f),internal_spacing);

        mVolumeLayers.add(new VolumeBarLayer(this));

        List<String> volumeLayerLabels = new ArrayList<String>();
        for (IMetricLayer layer : mVolumeLayers)
            volumeLayerLabels.add(layer.getShortName());
        mVolumeLayerAdapter = new MetricLayerButtonAdapter(volumeLayerLabels);
        mVolumeLayersContainer.setAdapter(mVolumeLayerAdapter);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle (this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        int currentStock = onLoadInstanceState(savedInstanceState);
        if (currentStock != -1)
        {
            mSymbol = mStockTabLayout.getTabAt(currentStock).getText().toString();
            mStockTabLayout.getTabAt(currentStock).select();
            launchTaskForInterval(mIntervalTabLayout.getSelectedTabPosition());
        }

        mStockTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ApplicationContext.setSelectedStock(tab.getPosition());
                mSymbol = tab.getText().toString();
                launchTaskForInterval(mIntervalTabLayout.getSelectedTabPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        mIntervalTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ;
                launchTaskForInterval(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private int onLoadInstanceState(Bundle inState)
    {
        List<Stock> watchList = ApplicationContext.getWatchList();
        for (Stock stock : watchList)
            mStockTabLayout.addTab(createTabForStock(stock));
        mWachListGeneration = ApplicationContext.getWatchListGeneration();

        if (inState == null)
            return ApplicationContext.getSelectedStock();

        mExtraStocks = Stock.loadFrom(inState);
        for (Stock stock : mExtraStocks)
            mStockTabLayout.addTab(createTabForStock(stock));

        int selectedStock = inState.getInt("SELECTED_STOCK", -1);
        if (selectedStock == -1)
            return ApplicationContext.getSelectedStock();

        return selectedStock;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Stock.saveTo(outState, mExtraStocks);
        outState.putInt("SELECTED_STOCK",  mStockTabLayout.getSelectedTabPosition());
    }

    // initialize default settins for any charts in this activity
    private void initializeChart(CombinedChart chart)
    {
        chart.setOnTouchListener(new ChartGestureHandler(this, chart, this));
        chart.setTouchEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawMarkers(false);
        chart.setDrawBorders(false);
        chart.getDescription().setText("");
        chart.getLegend().setEnabled(false);

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

        YAxis rightAxis =  chart.getAxisRight();
        rightAxis.setMinWidth(50);
        rightAxis.setMaxWidth(50);
        rightAxis.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        rightAxis.setDrawLabels(true);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setSpaceBottom(0);
        rightAxis.setSpaceTop(0);
    }

    private void launchTaskForInterval(int position)
    {
        DateTime start = null;
        QuoteQuery.IntervalUnit intervalUnit = null;
        int interval = 1;
        DateTime now = DateTime.now(DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST")));
        int hourOfDay = now.hourOfDay().get();
        int minuteOfDay = now.minuteOfDay().get();

        DateTime end =  now.withHourOfDay(16).withMinuteOfHour(0); // 4:00 pm is NYSE close
        int endDay = end.dayOfWeek().get();
        if (endDay == 6)
            end = end.minus(Duration.standardDays(1));
        else if (endDay == 7)
            end = end.minus(Duration.standardDays(2));
        else if (hourOfDay < 9 || ( hourOfDay == 9  && minuteOfDay < 40)) // make sure we have at least 2 quotes
        {
            if (endDay == 1)
                end = end.minus(Duration.standardDays(3));
            else
                end = end.minus(Duration.standardDays(1));
        }
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
                intervalUnit = QuoteQuery.IntervalUnit.Minute;
                break;
            case OneWeek:
                interval = 10;
                intervalUnit = QuoteQuery.IntervalUnit.Minute;
                start = start.minusWeeks(1);
                break;
            case OneMonth:
                intervalUnit = QuoteQuery.IntervalUnit.Day;
                start = start.minusMonths(1);
                break;
            case ThreeMonths:
                intervalUnit = QuoteQuery.IntervalUnit.Day;
                start = start.minusMonths(3);
                break;
            case OneYear:
                intervalUnit = QuoteQuery.IntervalUnit.Week;
                start = start.minusYears(1);
                break;
            case FiveYears:
                intervalUnit = QuoteQuery.IntervalUnit.Week;
                start = start.minusYears(5);
                break;
            case TenYears:
                intervalUnit = QuoteQuery.IntervalUnit.Month;
                start = start.minusYears(10);
                break;
        }
        mLastQuery.query = new QuoteQuery(mSymbol, start, end, intervalUnit, interval);
        mLastQuery.results = null;
        mQuoteTask = new QuoteQueryTask(mQuoteService, this);
        mQuoteTask.execute(mLastQuery.query);
    }

    public void onQuotesReceived(List<List<Quote>> manyQuotes)
    {
        boolean oneValidHquotes = false;
        Duration intervalDuration = mLastQuery.query.getIntervalAsDuration();

        if (mPriceChartData.getLineData() != null)
            mPriceChartData.getLineData().clearValues();
        if (mPriceChartData.getCandleData() != null)
            mPriceChartData.getCandleData().clearValues();
        if (mVolumeChartData.getBarData() != null)
            mVolumeChartData.getBarData().clearValues();

        for (int i = 0; i < manyQuotes.size(); i++)
        {
            List<Quote> quotes = manyQuotes.get(i);
            if (quotes == null || quotes.size() <= 1) {
                continue;
            }
            mLastQuery.results = quotes;

            Quote initialQuote = quotes.get(0);
            Quote finalQuote = quotes.get(quotes.size() -1);

            Duration missingStartDuration = new Duration(mLastQuery.query.start, initialQuote.getTime());
            Duration missingEndDuration = new Duration(finalQuote.getTime(), mLastQuery.query.end);

            int missingStartSteps = (int)(missingStartDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());
            int missingEndSteps = (int)(missingEndDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());

            for (IMetricLayer layer : mPriceLayers)
                layer.onDrawQuotes(quotes, missingStartSteps, missingEndSteps, mPriceChartData);

            for (IMetricLayer layer : mVolumeLayers)
                layer.onDrawQuotes(quotes,  missingStartSteps, missingEndSteps, mVolumeChartData);

            updateHeaderText(mLastQuery.results.get(0), mLastQuery.results.get(mLastQuery.results.size() -1));
            oneValidHquotes = true;
        }

        mPriceChart.setData(mPriceChartData);
        mPriceChart.notifyDataSetChanged();
        mPriceChart.fitScreen();

        mVolumeChart.setData(mVolumeChartData);
        mVolumeChart.notifyDataSetChanged();
        mVolumeChart.fitScreen();
        mLastQuery.complete = oneValidHquotes;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.quotes, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
       switch (item.getItemId()) {
           case R.id.search:
               Intent intent = new Intent(this, StockQueryActivity.class);
               startActivityForResult(intent, 1);
               break;
       }
       return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        int tabToSelect = -1;
        int newWatchListGen = ApplicationContext.getWatchListGeneration();
        if (mWachListGeneration != newWatchListGen)
        {
            Stock selectedStock = (Stock)mStockTabLayout.getTabAt(mStockTabLayout.getSelectedTabPosition()).getTag();
            List<Stock> watchList = ApplicationContext.getWatchList();

            mStockTabLayout.removeAllTabs();
            int i;
            for (i = 0; i < watchList.size(); ++i)
            {
                Stock stock = watchList.get(i);
                TabLayout.Tab tab = createTabForStock(stock);
                mStockTabLayout.addTab(tab);
                if (stock.equals(selectedStock))
                    tabToSelect = i;
            }

            for (Stock stock : mExtraStocks) {
                if (stock.equals(selectedStock))
                    tabToSelect = i;
                mStockTabLayout.addTab(createTabForStock(stock));
                ++i;
            }

            mWachListGeneration = newWatchListGen;
        }

        List<Stock> stocksChosen = Stock.loadFrom(data);
        if (stocksChosen == null || stocksChosen.isEmpty())
        {
            if (tabToSelect != -1)
                mStockTabLayout.getTabAt(tabToSelect).select();

            return;
        }

        Stock stockChosen = stocksChosen.get(0);
        int watchListIndex = ApplicationContext.getWatchListIndex(stockChosen);
        if (watchListIndex == -1) // stock not in watch list but viewing temporarily
        {
            mExtraStocks.add(stockChosen);
            TabLayout.Tab tab = createTabForStock(stockChosen);
            mStockTabLayout.addTab(tab);
            tab.select();
        }
        else if (mStockTabLayout.getSelectedTabPosition() != watchListIndex)
            mStockTabLayout.getTabAt(watchListIndex).select();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.quotes) {
            // Handle the camera action
        } else if (id == R.id.earnings) {

        } else if (id == R.id.notifications) {

        } else if (id == R.id.contribute) {

        } else if (id == R.id.donate) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void OnEntryHighlighted(Entry e)
    {
        if (!mLastQuery.complete)
            return;

        Quote quote = (Quote)e.getData();
        if (quote == null)
            return;

        Highlight h = new Highlight(e.getX(), 0, 0);
        h.setDataIndex(0);
        mPriceChart.highlightValue(h, false);
        mVolumeChart.highlightValue(h, false);
        updateHeaderText(mLastQuery.results.get(0), quote);
    }

    @Override
    public void OnHighlightFinished() {
        if (!mLastQuery.complete)
            return;

        mPriceChart.highlightValue(null);
        mVolumeChart.highlightValue(null);

        Quote lastQuery = mLastQuery.results.get(mLastQuery.results.size() -1);
        updateHeaderText(mLastQuery.results.get(0), lastQuery);
    }

    private static String percentToString(float percent)
    {
        float absVolume = Math.abs(percent);
        if (absVolume >= 1000000000) // you never know amiright?
            return String.format("%.2fB%%", percent / 1000000000.0f);
        else if (absVolume >= 1000000)  // you never know amiright?
            return String.format("%.2fM%%", percent / 1000000.0f);
        else if (absVolume >= 1000)
            return String.format("%.2fK%%", percent / 1000.0f);
        else
            return String.format("%.2f%%", percent);
    }

    private static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("MMM d y");
    private static DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("h:mm a z").withZone(DateTimeZone.getDefault());
    private void updateHeaderText(Quote startQuote, Quote endQuote)
    {
        float priceDiff = endQuote.getClose() - startQuote.getClose();
        float priceDiffPercent = (priceDiff / startQuote.getClose()) * 100.0f;

        int volumeDiff = endQuote.getVolume() - startQuote.getVolume();
        float volumeDiffPercent = ((float)volumeDiff / (float)startQuote.getVolume()) * 100.0f;

        DateTime endTime = endQuote.getTime();
        mDateText.setText(dateFormatter.print(endTime));
        mTimeText.setText(timeFormatter.print(endTime));
        mPriceText.setText(PriceMetric.valueToString(endQuote.getClose()));
        mPriceChangeText.setText(String.format("%s (%s)", PriceMetric.valueDiffToString(priceDiff), percentToString(priceDiffPercent)));
        mVolumeText.setText(VolumeMetric.valueToString(endQuote.getVolume()));
        mVolumeChangeText.setText(String.format("%s (%s)", VolumeMetric.valueDiffToString(volumeDiff), percentToString(volumeDiffPercent)));
    }


    private TabLayout.Tab createTabForStock(Stock stock)
    {
        TabLayout.Tab tab = mStockTabLayout.newTab();
        tab.setText(stock.getSymbol());
        tab.setTag(stock);
        return tab;
    }
}

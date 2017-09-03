package com.magellan.magellan;

import java.util.List;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.util.TypedValue;
import android.util.Log;
import android.os.Bundle;
import android.view.MenuInflater;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
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
import com.magellan.magellan.quote.IQuoteService;
import com.magellan.magellan.quote.Quote;
import com.magellan.magellan.quote.QuoteQuery;
import com.magellan.magellan.quote.IQuoteQueryListener;
import com.magellan.magellan.quote.QuoteQueryTask;
import com.magellan.magellan.service.alphavantage.AlphaVantageService;
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

    private static int CHART_MARGIN = 10;
    private static int CHART_SPACING = 5;

    private int mWachListGeneration;
    private String mSymbol;
    private TextView mDateText;
    private TextView mTimeText;

    private TabLayout mStockTabLayout;
    private List<Stock> mExtraStocks = new ArrayList<Stock>();

    private TextView mPriceText;
    private TextView mVolumeText;
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

    private ProgressBar mPriceLoadProgress;
    private ProgressBar mVolumeLoadProgress;
    private QuoteQueryTask mQuoteTask;
    private QueryContext mLastQueryContext = new QueryContext();
    private Quote mPeriodQuote;

    private List<IMetricLayer> mPriceLayers = new ArrayList<IMetricLayer>();
    private List<IMetricLayer> mVolumeLayers = new ArrayList<IMetricLayer>();

    private IQuoteService mQuoteService = new AlphaVantageService();

    private class QueryContext
    {
        QuoteQuery query;
        List<Quote> results;
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

        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        mPriceLayersContainer = (RecyclerView) priceCard.findViewById(R.id.layers);
        mPriceLayersContainer.setLayoutManager(new LinearLayoutManager(this, isPortrait ? LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL, false));

        float internal_spacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());

        mPriceChart = (CombinedChart) priceCard.findViewById(R.id.chart);
        mPriceLoadProgress = (ProgressBar) priceCard.findViewById(R.id.progress);
        mPriceChartData = new CombinedData();
        initializeChart(mPriceChart);
        YAxis priceAxisRight = mPriceChart.getAxisRight();
        priceAxisRight.setValueFormatter(new PriceMetric.AxisValueFormatter());
        priceAxisRight.setLabelCount(isPortrait ? 9 : 6, true);
        mPriceChart.setViewPortOffsets(0,internal_spacing,(priceAxisRight.getTextSize() * 4) + internal_spacing,internal_spacing);

        mPriceLayers.add(new PriceCandleLayer(this));
        mPriceLayers.add(new PriceLineLayer(this));

        List<String> priceLayerLabels = new ArrayList<String>();
        for (IMetricLayer layer : mPriceLayers)
            priceLayerLabels.add(layer.getShortName());
        mPriceLayerAdapter = new MetricLayerButtonAdapter(priceLayerLabels);
        mPriceLayersContainer.setAdapter(mPriceLayerAdapter);

        View volumeCard = findViewById(R.id.volume_card);
        mVolumeText = (TextView) volumeCard.findViewById(R.id.value);
        mIntervalTabLayout = (TabLayout) findViewById(R.id.interval_tabs);

        mVolumeLayersContainer = (RecyclerView) volumeCard.findViewById(R.id.layers);
        mVolumeLayersContainer.setLayoutManager(new LinearLayoutManager(this, isPortrait ? LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL, false));

        mVolumeChart = (CombinedChart) volumeCard.findViewById(R.id.chart);
        mVolumeLoadProgress = (ProgressBar) volumeCard.findViewById(R.id.progress);
        mVolumeChartData = new CombinedData();
        initializeChart(mVolumeChart);
        YAxis volAxisRight = mVolumeChart.getAxisRight();
        volAxisRight.setValueFormatter(new VolumeMetric.AxisValueFormatter(2));
        volAxisRight.setLabelCount(isPortrait ? 7 : 3, true);
        mVolumeChart.setViewPortOffsets(0,internal_spacing,(volAxisRight.getTextSize() * 4) + internal_spacing,internal_spacing);

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

        int selectedInterval = inState.getInt("SELECTED_INTERVAL", -1);
        if (selectedInterval != -1)
            mIntervalTabLayout.getTabAt(selectedInterval).select();

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
        outState.putInt("SELECTED_INTERVAL", mIntervalTabLayout.getSelectedTabPosition());
    }

    // initialize default settins for any charts in this activity
    private void initializeChart(CombinedChart chart)
    {
        chart.setNoDataText("");
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
        rightAxis.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        rightAxis.setGridColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        rightAxis.setDrawLabels(true);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setSpaceBottom(0);
        rightAxis.setSpaceTop(0);
    }

    private void launchTaskForInterval(int position)
    {
        QuoteQuery.Period [] quotePeriods = QuoteQuery.Period.values();
        if (position <0 || position > quotePeriods.length)
        {
            Log.e("Magellan", "Encountered Unknown Duration Tab Index");
            return;
        }

        mPriceLoadProgress.setVisibility(View.VISIBLE);
        mVolumeLoadProgress.setVisibility(View.VISIBLE);
        QuoteQuery.Period period = quotePeriods[position];

        QuoteQuery.Interval interval = QuoteQuery.Interval.FiveMinutes;
        mLastQueryContext.complete = false;
        switch (period)
        {
            case OneDay:
                interval = QuoteQuery.Interval.FiveMinutes;
                break;
            case OneWeek:
                interval = QuoteQuery.Interval.FifteenMinutes;
                break;
            case OneMonth:
                interval = QuoteQuery.Interval.ThirtyMinutes;
                break;
            case ThreeMonths:
                interval = QuoteQuery.Interval.OneDay;
                break;
            case OneYear:
                interval = QuoteQuery.Interval.OneDay;
                break;
            case FiveYears:
                interval = QuoteQuery.Interval.OneWeek;
                break;
            case TenYears:
                interval = QuoteQuery.Interval.OneMonth;
                break;
        }
        mLastQueryContext.query = new QuoteQuery(mSymbol, period, interval);
        mLastQueryContext.results = null;
        mQuoteTask = new QuoteQueryTask(this, mQuoteService, this);
        mQuoteTask.execute(mLastQueryContext.query);
    }

    public void onQuotesReceived(List<List<Quote>> manyQuotes)
    {
        boolean oneValidQuote = false;
        Duration intervalDuration = mLastQueryContext.query.getIntervalAsDuration();

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
            mLastQueryContext.results = quotes;

            Quote initialQuote = quotes.get(0);
            Quote finalQuote = quotes.get(quotes.size() -1);

            Duration missingStartDuration = new Duration(mLastQueryContext.query.getStart(), initialQuote.getTime());
            Duration missingEndDuration = new Duration(finalQuote.getTime(), mLastQueryContext.query.getEnd());

            int missingStartSteps = (int)(missingStartDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());
            int missingEndSteps = (int)(missingEndDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());

            long totalVolume = 0;
            float lowestPrice = Float.MAX_VALUE;
            float highestPrice = -Float.MAX_VALUE;
            for (Quote q : quotes)
            {
                totalVolume += q.getVolume();
                if (lowestPrice > q.getLow())
                    lowestPrice = q.getLow();

                if (highestPrice < q.getHigh())
                    highestPrice = q.getHigh();
            }
            mPeriodQuote = new Quote(null,initialQuote.getOpen(), finalQuote.getClose(), lowestPrice, highestPrice, totalVolume);

            for (IMetricLayer layer : mPriceLayers)
                layer.onDrawQuotes(quotes, missingStartSteps, missingEndSteps, mPriceChartData);

            for (IMetricLayer layer : mVolumeLayers)
                layer.onDrawQuotes(quotes,  missingStartSteps, missingEndSteps, mVolumeChartData);

            updateHeaderText(mPeriodQuote);
            oneValidQuote = true;
        }

        mPriceLoadProgress.setVisibility(View.GONE);
        mVolumeLoadProgress.setVisibility(View.GONE);

        mPriceChart.setData(mPriceChartData);
        mPriceChart.notifyDataSetChanged();
        mPriceChart.fitScreen();

        mVolumeChart.setData(mVolumeChartData);
        mVolumeChart.notifyDataSetChanged();
        mVolumeChart.fitScreen();
        mLastQueryContext.complete = oneValidQuote;
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
        if (!mLastQueryContext.complete)
            return;

        Quote quote = (Quote)e.getData();
        if (quote == null)
            return;

        Highlight h = new Highlight(e.getX(), 0, 0);
        h.setDataIndex(0);
        mPriceChart.highlightValue(h, false);
        mVolumeChart.highlightValue(h, false);
        updateHeaderText(quote);
    }

    @Override
    public void OnHighlightFinished() {
        if (!mLastQueryContext.complete)
            return;

        mPriceChart.highlightValue(null);
        mVolumeChart.highlightValue(null);

        updateHeaderText(mPeriodQuote);
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

    private void updateHeaderText(Quote quote)
    {
        DateTime endTime = quote.getTime();
        mDateText.setText(dateFormatter.print(endTime));
        mTimeText.setText(timeFormatter.print(endTime));
        mPriceText.setText(Html.fromHtml(String.format("<b>H</b> %s  <b>L</b> %s  <b>O</b> %s  <b>C</b> %s",PriceMetric.valueToString(quote.getHigh()), PriceMetric.valueToString(quote.getLow()), PriceMetric.valueToString(quote.getOpen()), PriceMetric.valueToString(quote.getClose()))));
        mVolumeText.setText(Html.fromHtml("<b>" + VolumeMetric.valueToString(quote.getVolume())+ "</b>"));
    }


    private TabLayout.Tab createTabForStock(Stock stock)
    {
        TabLayout.Tab tab = mStockTabLayout.newTab();
        tab.setText(stock.getSymbol());
        tab.setTag(stock);
        return tab;
    }
}

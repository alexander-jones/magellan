package com.magellan.magellan;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.util.Log;
import android.os.Bundle;
import android.view.MenuInflater;
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
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.magellan.magellan.metric.ILineDataSetStyler;
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
import com.magellan.magellan.stock.Stock;
import com.magellan.magellan.stock.StockQueryActivity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

public class QuotesActivity extends AppCompatActivity
        implements IQuoteQueryListener, ChartGestureHandler.OnHighlightListener{


    public class LineDataSetStyler implements ILineDataSetStyler {

        @Override
        public void onApply(LineDataSet lineSet)
        {
            lineSet.setDrawIcons(false);
            lineSet.setHighlightEnabled(true);
            lineSet.disableDashedLine();
            lineSet.enableDashedHighlightLine(10f, 5f, 0f);
            lineSet.setHighLightColor(ContextCompat.getColor(QuotesActivity.this, R.color.colorPrimary));
            lineSet.setColor(ContextCompat.getColor(QuotesActivity.this, R.color.colorAccentPrimary));
            lineSet.setFillColor(ContextCompat.getColor(QuotesActivity.this, R.color.colorAccentPrimary));
            lineSet.setLineWidth(1f);
            lineSet.setValueTextSize(9f);
            lineSet.setDrawFilled(true);
            lineSet.setFormLineWidth(1f);
            lineSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            lineSet.setFormSize(15.f);
            lineSet.setDrawCircles(false);
            lineSet.setDrawValues(false);
        }
    }

    private LineDataSetStyler mLineDataStyler = new LineDataSetStyler();

    private boolean mUseWatchlist;
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

    private ProgressBar mPriceLoadProgress;
    private ProgressBar mVolumeLoadProgress;
    private QuoteQueryTask mQuoteTask;
    private QueryContext mLastQueryContext = new QueryContext();
    private Quote mPeriodQuote;

    private List<IMetricLayer> mPriceLayers = new ArrayList<IMetricLayer>();
    private List<IMetricLayer> mVolumeLayers = new ArrayList<IMetricLayer>();


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
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mDateText = (TextView) findViewById(R.id.date);
        mTimeText = (TextView) findViewById(R.id.time);
        mStockTabLayout = (TabLayout) findViewById(R.id.stock_tabs);

        View priceCard = findViewById(R.id.price_card);
        mPriceText = (TextView) priceCard.findViewById(R.id.value);

        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        mPriceLayersContainer = (RecyclerView) priceCard.findViewById(R.id.layers);
        mPriceLayersContainer.setLayoutManager(new LinearLayoutManager(this, isPortrait ? LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL, false));

        float internal_spacing = getResources().getDimension(R.dimen.spacing_internal);

        mPriceChart = (CombinedChart) priceCard.findViewById(R.id.chart);
        mPriceLoadProgress = (ProgressBar) priceCard.findViewById(R.id.progress);
        mPriceChartData = new CombinedData();
        initializeChart(mPriceChart);
        YAxis priceAxisRight = mPriceChart.getAxisRight();
        priceAxisRight.setValueFormatter(new PriceMetric.AxisValueFormatter());
        mPriceChart.setViewPortOffsets(0, internal_spacing, (priceAxisRight.getTextSize() * 4) + internal_spacing, internal_spacing);

        mPriceLayers.add(new PriceCandleLayer(this));
        mPriceLayers.add(new PriceLineLayer(mLineDataStyler));

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
        mVolumeChart.setViewPortOffsets(0, internal_spacing,(volAxisRight.getTextSize() * 4) + internal_spacing, internal_spacing);

        mVolumeLayers.add(new VolumeBarLayer(this));

        List<String> volumeLayerLabels = new ArrayList<String>();
        for (IMetricLayer layer : mVolumeLayers)
            volumeLayerLabels.add(layer.getShortName());
        mVolumeLayerAdapter = new MetricLayerButtonAdapter(volumeLayerLabels);
        mVolumeLayersContainer.setAdapter(mVolumeLayerAdapter);

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
        int selection;
        List<Stock> stocks;
        if (inState == null)
        {
            Intent intent = getIntent();
            selection = intent.getIntExtra("WATCHLIST_ITEM", -1);
            mUseWatchlist = selection != -1;
            if (mUseWatchlist)
                stocks = ApplicationContext.getWatchList();
            else
            {
                stocks = Stock.loadFrom(intent);
                selection = 0;
            }
        }
        else
        {
            selection = inState.getInt("SELECTED_STOCK");
            mUseWatchlist = inState.getBoolean("USE_WATCHLIST", false);
            mIntervalTabLayout.getTabAt(inState.getInt("SELECTED_INTERVAL")).select();

            if (mUseWatchlist)
                stocks = ApplicationContext.getWatchList();
            else
                stocks = Stock.loadFrom(inState);
        }

        for (Stock stock : stocks)
            mStockTabLayout.addTab(createTabForStock(stock));

        return selection;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        Stock.saveTo(outState, mExtraStocks);
        outState.putInt("SELECTED_STOCK",  mStockTabLayout.getSelectedTabPosition());
        outState.putInt("SELECTED_INTERVAL", mIntervalTabLayout.getSelectedTabPosition());
        outState.putBoolean("USE_WATCHLIST", mUseWatchlist);
        if (!mUseWatchlist)
        {
            List<Stock> stocks = new ArrayList<Stock>();
            for (int i =0; i < mStockTabLayout.getTabCount(); ++i)
                stocks.add((Stock)mStockTabLayout.getTabAt(i).getTag());

            Stock.saveTo(outState, stocks);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
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
            case android.R.id.home:
                finish();
                break;
            case R.id.settings:
                // TODO implement settings activity / fragment and launch here
                break;
        }
        return true;
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
        mQuoteTask = new QuoteQueryTask(this, ApplicationContext.getQuoteService(), this);
        mQuoteTask.execute(mLastQueryContext.query);
    }

    @Override
    public void onQuotesReceived(List<QuoteQuery> queries, List<List<Quote>> manyQuotes)
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

            Duration missingStartDuration = new Duration(mLastQueryContext.query.getStart(), initialQuote.time);
            Duration missingEndDuration = new Duration(finalQuote.time, mLastQueryContext.query.getEnd());

            int missingStartSteps = (int)(missingStartDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());
            int missingEndSteps = (int)(missingEndDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());

            long totalVolume = 0;
            float lowestPrice = Float.MAX_VALUE;
            float highestPrice = -Float.MAX_VALUE;
            for (Quote q : quotes)
            {
                totalVolume += q.volume;
                if (lowestPrice > q.low)
                    lowestPrice = q.low;

                if (highestPrice < q.high)
                    highestPrice = q.high;
            }
            mPeriodQuote = new Quote(null,initialQuote.open, finalQuote.close, lowestPrice, highestPrice, totalVolume);

            for (IMetricLayer layer : mPriceLayers)
                layer.onDrawQuotes(quotes, missingStartSteps, missingEndSteps, mPriceChartData);

            for (IMetricLayer layer : mVolumeLayers)
                layer.onDrawQuotes(quotes,  missingStartSteps, missingEndSteps, mVolumeChartData);

            // enable for center line
            /*float startingOpen = initialQuote.open;
            ArrayList<Entry> missingPriceValues = new ArrayList<Entry>();
            missingPriceValues.add(new Entry(0, startingOpen, null));
            missingPriceValues.add(new Entry(quotes.size(), startingOpen, null));

            // enable for center line
            LineDataSet lineSet = new LineDataSet(missingPriceValues, "");
            lineSet.setDrawIcons(false);
            lineSet.setHighlightEnabled(false);
            lineSet.enableDashedLine(10f, 10f, 0f);
            lineSet.setColor(Color.WHITE);
            lineSet.setFillColor(Color.WHITE);
            lineSet.setLineWidth(1f);
            lineSet.setDrawCircles(false);
            lineSet.setDrawValues(false);

            LineData data = mPriceChartData.getLineData();
            if (data == null){
                ArrayList<ILineDataSet> priceDataSets = new ArrayList<ILineDataSet>();
                priceDataSets.add(lineSet);
                data = new LineData(priceDataSets);
                mPriceChartData.setData(data);
            }
            else {
                data.addDataSet(lineSet);
            }*/

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
        mDateText.setText(dateFormatter.print(quote.time));
        mTimeText.setText(timeFormatter.print(quote.time));
        mPriceText.setText(Html.fromHtml(String.format("<b>H</b> %s  <b>L</b> %s  <b>O</b> %s  <b>C</b> %s",PriceMetric.valueToString(quote.high), PriceMetric.valueToString(quote.low), PriceMetric.valueToString(quote.open), PriceMetric.valueToString(quote.close))));
        mVolumeText.setText(Html.fromHtml("<b>" + VolumeMetric.valueToString(quote.volume)+ "</b>"));
    }


    private TabLayout.Tab createTabForStock(Stock stock)
    {
        TabLayout.Tab tab = mStockTabLayout.newTab();
        tab.setText(stock.getSymbol());
        tab.setTag(stock);
        return tab;
    }
}

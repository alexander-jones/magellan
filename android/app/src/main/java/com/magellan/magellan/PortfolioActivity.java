package com.magellan.magellan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.magellan.magellan.WatchlistStockAdapter;
import com.magellan.magellan.metric.ILineDataSetStyler;
import com.magellan.magellan.metric.MetricLayerButtonAdapter;
import com.magellan.magellan.metric.price.PriceLineLayer;
import com.magellan.magellan.metric.price.PriceMetric;
import com.magellan.magellan.metric.volume.VolumeMetric;
import com.magellan.magellan.quote.IQuoteQueryListener;
import com.magellan.magellan.quote.Quote;
import com.magellan.magellan.quote.QuoteQuery;
import com.magellan.magellan.quote.QuoteQueryTask;
import com.magellan.magellan.stock.Stock;
import com.magellan.magellan.stock.StockQueryActivity;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PortfolioActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, IQuoteQueryListener {


    public class LineDataSetStyler implements ILineDataSetStyler {

        private int mLineColor;
        public LineDataSetStyler(int color)
        {
            mLineColor = color;
        }

        @Override
        public void onApply(LineDataSet lineSet) {
            lineSet.setDrawCircles(false);
            lineSet.setDrawValues(false);
            lineSet.setDrawFilled(false);
            lineSet.setDrawIcons(false);
            lineSet.setHighlightEnabled(false);
            lineSet.disableDashedLine();
            lineSet.setColor(mLineColor);
            lineSet.setLineWidth(1f);
        }
    }

    private LineDataSetStyler mPriceUpLineStyler;
    private LineDataSetStyler mPriceDownLineStyler;

    private int mWachListGeneration;
    private DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    private RecyclerView mWatchListContainer;
    private WatchlistStockAdapter mWatchListAdapter;
    private List<Stock> mWatchListItems = new ArrayList<Stock>();
    private List<WatchListStockContext> mWatchListWatchListStockContexts = new ArrayList<WatchListStockContext>();
    private HashMap<QuoteQuery, Stock> stockQueriesInFlight = new HashMap<QuoteQuery, Stock>();
    private HashMap<QuoteQuery, Stock> indexQueriesInFlight = new HashMap<QuoteQuery, Stock>();

    private CombinedChart mIndexChart;
    private List<Stock> mIndexes = new ArrayList<Stock>();
    private List<IndexContext> mIndexContexts = new ArrayList<IndexContext>();
    private CombinedData mIndexData = new CombinedData();
    private LinearLayout.LayoutParams mIndexPriceLayoutParams;
    private LinearLayout mIndexPriceContainer;
    private RecyclerView mIndexItemButtonContainer;
    private MetricLayerButtonAdapter mIndexItemButtonAdapter;
    private List<String> mIndexItemLabels = new ArrayList<String>();
    private List<Integer> mIndexColors = new ArrayList<Integer>();

    private class WatchListStockContext
    {
        public WatchListStockContext()
        {
            layer = new PriceLineLayer(mPriceUpLineStyler);
            allData = new CombinedData();
            quotes = null;
        }
        PriceLineLayer layer;
        CombinedData allData;
        List<Quote> quotes;
    }

    private class IndexContext
    {
        public IndexContext(TextView priceTextView, int color)
        {
            price = priceTextView;
            layer = new PriceLineLayer(new LineDataSetStyler(color));
            original_quotes = null;
            normalized_quotes = null;
        }

        TextView price;
        PriceLineLayer layer;
        List<Quote> original_quotes;
        List<Quote> normalized_quotes;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationContext.init(this);

        setContentView(R.layout.activity_portfolio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mIndexPriceLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mIndexPriceLayoutParams.setMargins(0, (int)getResources().getDimension(R.dimen.spacing_internal), (int)getResources().getDimension(R.dimen.spacing_external),0);

        mIndexes =  Arrays.asList(new Stock("SPX", "S&P 500", "N/A", "Index"), new Stock("DJIA", "Dow Jones", "N/A", "Index"), new Stock("IXIC", "NASDAQ", "N/A", "Index"));
        int [] indexColors = {ContextCompat.getColor(this, R.color.colorAccentPrimary), ContextCompat.getColor(this, R.color.colorAccentSecondary), ContextCompat.getColor(this, R.color.colorAccentTertiary)};
        for (int i =0; i < mIndexes.size(); ++i)
        {
            mIndexItemLabels.add(mIndexes.get(i).getCompany());
            mIndexColors.add(indexColors[i]);
        }

        mIndexPriceContainer = (LinearLayout) findViewById(R.id.price_container);
        mIndexItemButtonContainer = (RecyclerView) findViewById(R.id.index_layers);
        mIndexItemButtonAdapter = new MetricLayerButtonAdapter(mIndexItemLabels, mIndexColors);
        mIndexItemButtonContainer.setAdapter(mIndexItemButtonAdapter);

        mIndexChart = (CombinedChart)findViewById(R.id.index_chart);
        for (int i =0; i < mIndexes.size(); ++i)
        {
            Stock index = mIndexes.get(i);
            TextView textView = createPriceTextView(indexColors[i]);
            mIndexPriceContainer.addView(textView, mIndexPriceLayoutParams);
            mIndexContexts.add(new IndexContext(textView, indexColors[i]));
            QuoteQuery query = new QuoteQuery(index.getSymbol(), QuoteQuery.Period.OneDay, QuoteQuery.Interval.FiveMinutes);
            indexQueriesInFlight.put(query, index);
            QuoteQueryTask task = new QuoteQueryTask(this, ApplicationContext.getQuoteService(), this);
            task.execute(query);
        }

        ApplicationContext.initializeSimpleChart(this, mIndexChart);

        mPriceUpLineStyler = new LineDataSetStyler(ContextCompat.getColor(PortfolioActivity.this, R.color.colorPriceUp));
        mPriceDownLineStyler = new LineDataSetStyler(ContextCompat.getColor(PortfolioActivity.this, R.color.colorPriceDown));
        mWatchListItems.addAll(ApplicationContext.getWatchList());
        for (int i =0; i < mWatchListItems.size(); ++i)
            mWatchListWatchListStockContexts.add(new WatchListStockContext());

        mWachListGeneration = ApplicationContext.getWatchListGeneration();
        mWatchListContainer = (RecyclerView)findViewById(R.id.watchlist_container);
        mWatchListAdapter = new WatchlistStockAdapter(mWatchListItems, this);
        mWatchListContainer.setAdapter(mWatchListAdapter);

        ItemTouchHelper ith = new ItemTouchHelper(
            new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
                int dragFrom = -1;
                int dragTo = -1;
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    final int fromPos = viewHolder.getAdapterPosition();
                    final int toPos = target.getAdapterPosition();

                    if(dragFrom == -1) {
                        dragFrom =  fromPos;
                    }
                    dragTo = toPos;
                    Collections.swap(mWatchListItems, fromPos, toPos);
                    Collections.swap(mWatchListWatchListStockContexts, fromPos, toPos);
                    mWatchListAdapter.notifyItemMoved(fromPos, toPos);
                    return true;
                }
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    ApplicationContext.removeFromWatchList(position);
                    mWatchListItems.remove(position);
                    mWatchListWatchListStockContexts.remove(position);
                    mWatchListAdapter.notifyItemRemoved(position);
                }

                @Override
                public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    super.clearView(recyclerView, viewHolder);

                    if(dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                        ApplicationContext.moveItemInWatchlist(dragFrom, dragTo);
                    }

                    dragFrom = dragTo = -1;
                }
            });

        ith.attachToRecyclerView(mWatchListContainer);

        for (Stock stock : mWatchListItems)
            lanchTaskForStock(stock);
    }

    private TextView createPriceTextView(int color)
    {
        TextView ret = new TextView(this);
        ret.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.small_header_text_size));
        ret.setTextColor(color);
        return ret;
    }

    @Override
    public void onQuotesReceived(List<QuoteQuery> queries, List<List<Quote>> manyQuotes)
    {
        for (int i = 0; i < queries.size(); ++i)
        {
            List<Quote> quotes = manyQuotes.get(i);
            if (quotes == null || quotes.size() <= 1) {
                continue;
            }

            QuoteQuery query = queries.get(i);
            Duration intervalDuration = query.getIntervalAsDuration();

            Quote initialQuote = quotes.get(0);
            Quote finalQuote = quotes.get(quotes.size() -1);

            Duration missingStartDuration = new Duration(query.start, initialQuote.time);
            Duration missingEndDuration = new Duration(finalQuote.time, query.end);

            int missingStartSteps = (int)(missingStartDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());
            int missingEndSteps = (int)(missingEndDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());

            float lowestPrice = Float.MAX_VALUE;
            float highestPrice = -Float.MAX_VALUE;
            for (Quote q : quotes)
            {
                if (lowestPrice > q.low)
                    lowestPrice = q.low;

                if (highestPrice < q.high)
                    highestPrice = q.high;
            }

            Stock stock = indexQueriesInFlight.remove(query);
            if (stock == null)
            {
                stock = stockQueriesInFlight.remove(query);
                int position = mWatchListItems.indexOf(stock);
                WatchListStockContext chartCtx = mWatchListWatchListStockContexts.get(position);
                chartCtx.quotes = quotes;
                if (chartCtx.allData.getLineData() != null)
                    chartCtx.allData.getLineData().clearValues();

                WatchlistStockAdapter.ViewHolder vh = (WatchlistStockAdapter.ViewHolder)mWatchListContainer.findViewHolderForAdapterPosition(position);
                vh.value.setText(PriceMetric.valueToString(finalQuote.close));
                if (finalQuote.close > initialQuote.open) {
                    chartCtx.layer.setStyler(mPriceUpLineStyler);
                    vh.value.setTextColor(ContextCompat.getColor(this, R.color.colorPriceUp));
                }
                else {
                    chartCtx.layer.setStyler(mPriceDownLineStyler);
                    vh.value.setTextColor(ContextCompat.getColor(this, R.color.colorPriceDown));
                }

                // draw center line
                float startingOpen = initialQuote.open;
                ArrayList<Entry> centerLineValues = new ArrayList<Entry>();
                centerLineValues.add(new Entry(0, startingOpen, null));
                centerLineValues.add(new Entry(quotes.size() + missingStartSteps + missingEndSteps - 1, startingOpen, null));

                LineDataSet centerLineSet = new LineDataSet(centerLineValues, "");
                centerLineSet.setDrawIcons(false);
                centerLineSet.setHighlightEnabled(false);
                centerLineSet.enableDashedLine(10f, 10f, 0f);
                centerLineSet.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
                centerLineSet.setFillColor(ContextCompat.getColor(this, R.color.colorPrimary));
                centerLineSet.setLineWidth(1f);
                centerLineSet.setDrawCircles(false);
                centerLineSet.setDrawValues(false);

                LineData data = chartCtx.allData.getLineData();
                if (data == null){
                    ArrayList<ILineDataSet> priceDataSets = new ArrayList<ILineDataSet>();
                    priceDataSets.add(centerLineSet);
                    data = new LineData(priceDataSets);
                }
                else
                    data.addDataSet(centerLineSet);
                chartCtx.allData.setData(data);

                chartCtx.layer.onDrawQuotes(quotes, 0, 0, chartCtx.allData); // don't draw missing start / end steps when center line will pad data for us

                float fromCenterToExtent = Math.max(highestPrice - startingOpen, startingOpen - lowestPrice);
                vh.chart.getAxisLeft().setAxisMaximum(startingOpen + fromCenterToExtent);
                vh.chart.getAxisLeft().setAxisMinimum(startingOpen - fromCenterToExtent);
                vh.chart.setData(chartCtx.allData);
                vh.chart.notifyDataSetChanged();
                vh.chart.fitScreen();
            }
            else
            {
                int position = mIndexes.indexOf(stock);
                IndexContext chartCtx = mIndexContexts.get(position);

                float range = highestPrice - lowestPrice;
                chartCtx.normalized_quotes = new ArrayList<Quote>(quotes.size());
                chartCtx.original_quotes = quotes;
                for (Quote q : quotes)
                {
                    float open = (q.open - lowestPrice) / range;
                    float close = (q.open - lowestPrice) / range;
                    float low = (q.open - lowestPrice) / range;
                    float high = (q.open - lowestPrice) / range;
                    chartCtx.normalized_quotes.add(new Quote(q.time, open, close, low, high, q.volume));
                }

                chartCtx.price.setText(PriceMetric.valueToString(finalQuote.close));
                chartCtx.layer.onDrawQuotes(chartCtx.normalized_quotes, missingStartSteps, missingEndSteps, mIndexData);

                mIndexChart.setData(mIndexData);
                mIndexChart.notifyDataSetChanged();
                mIndexChart.fitScreen();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        int newWatchListGen = ApplicationContext.getWatchListGeneration();
        if (mWachListGeneration != newWatchListGen)
        {
            List<Stock> newWatchList = ApplicationContext.getWatchList();
            for (int i =0; i < mWatchListItems.size(); ++i) {
                if (i >= newWatchList.size() || !mWatchListItems.get(i).equals(newWatchList.get(i)))
                {
                    mWatchListItems.remove(i);
                    mWatchListWatchListStockContexts.remove(i);
                    mWatchListAdapter.notifyItemRemoved(i);
                }
            }

            for (int i = mWatchListItems.size(); i < newWatchList.size(); ++i) {
                Stock stock = newWatchList.get(i);
                mWatchListItems.add(stock);
                mWatchListWatchListStockContexts.add(new WatchListStockContext());
                mWatchListAdapter.notifyItemRangeInserted(mWatchListItems.size() -1, 1);
                lanchTaskForStock(stock);
            }
        }

        List<Stock> stocksChosen = Stock.loadFrom(data);
        if (stocksChosen != null)
        {
            Intent intent = new Intent(this, QuotesActivity.class);
            Stock.saveTo(intent, stocksChosen);
            startActivityForResult(intent, 1);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.portfolio, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.portfolio) {
        } else if (id == R.id.notifications) {

        } else if (id == R.id.contribute) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, QuotesActivity.class);
        intent.putExtra("WATCHLIST_ITEM", mWatchListItems.indexOf(view.getTag()));
        startActivityForResult(intent, 1);
    }

    private void lanchTaskForStock(Stock stock)
    {
        QuoteQuery query = new QuoteQuery(stock.getSymbol(), QuoteQuery.Period.OneDay, QuoteQuery.Interval.FiveMinutes);
        stockQueriesInFlight.put(query, stock);

        QuoteQueryTask task = new QuoteQueryTask(this, ApplicationContext.getQuoteService(), this);
        task.execute(query);
    }
}

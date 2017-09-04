package com.magellan.magellan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.magellan.magellan.metric.WatchlistStockAdapter;
import com.magellan.magellan.metric.price.PriceLineLayer;
import com.magellan.magellan.metric.price.PriceMetric;
import com.magellan.magellan.metric.volume.VolumeMetric;
import com.magellan.magellan.quote.IQuoteQueryListener;
import com.magellan.magellan.quote.Quote;
import com.magellan.magellan.quote.QuoteQuery;
import com.magellan.magellan.quote.QuoteQueryTask;
import com.magellan.magellan.stock.Stock;
import com.magellan.magellan.stock.StockQueryActivity;

import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PortfolioActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, IQuoteQueryListener {

    private int mWachListGeneration;
    private DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    private RecyclerView mWatchListContainer;
    private WatchlistStockAdapter mWatchListAdapter;
    private List<Stock> mWatchListItems = new ArrayList<Stock>();
    private List<ChartContext> mWatchListChartContexts = new ArrayList<ChartContext>();
    private HashMap<QuoteQuery, Stock> queriesInFlight = new HashMap<QuoteQuery, Stock>();

    private TextView mPortfolioChartPrice;
    private CombinedChart mPortfolioChart;
    private ChartContext mPortfolioChartContext;
    private class ChartContext
    {
        public ChartContext(List<Quote> qs)
        {
            layer = new PriceLineLayer(PortfolioActivity.this);
            data = new CombinedData();
            quotes = qs;
        }
        PriceLineLayer layer;
        CombinedData data;
        List<Quote> quotes;
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

        mPortfolioChart = (CombinedChart)findViewById(R.id.portfolio_chart);
        mPortfolioChartPrice = (TextView)findViewById(R.id.portfolio_price);
        mPortfolioChartContext = new ChartContext(new ArrayList<Quote>());
        ApplicationContext.initializeSimpleChart(mPortfolioChart);

        mWatchListItems.addAll(ApplicationContext.getWatchList());
        for (int i =0; i < mWatchListItems.size(); ++i)
            mWatchListChartContexts.add(new ChartContext(null));

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
                    Collections.swap(mWatchListChartContexts, fromPos, toPos);
                    mWatchListAdapter.notifyItemMoved(fromPos, toPos);
                    return true;
                }
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    ApplicationContext.removeFromWatchList(position);
                    mWatchListItems.remove(position);
                    mWatchListChartContexts.remove(position);
                    mWatchListAdapter.notifyItemRemoved(position);

                    mPortfolioChartContext.quotes.clear();
                    for (ChartContext ctx : mWatchListChartContexts)
                    {
                        if (ctx.quotes != null)
                            addToPortfolioChart(ctx.quotes);
                    }
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
            Stock stock = queriesInFlight.remove(query);
            int position = mWatchListItems.indexOf(stock);
            ChartContext chartCtx = mWatchListChartContexts.get(position);
            WatchlistStockAdapter.ViewHolder vh = (WatchlistStockAdapter.ViewHolder)mWatchListContainer.findViewHolderForAdapterPosition(position);

            chartCtx.quotes = quotes;
            if (chartCtx.data.getLineData() != null)
                chartCtx.data.getLineData().clearValues();

            Quote initialQuote = quotes.get(0);
            Quote finalQuote = quotes.get(quotes.size() -1);

            Duration missingStartDuration = new Duration(query.getStart(), initialQuote.time);
            Duration missingEndDuration = new Duration(finalQuote.time, query.getEnd());

            int missingStartSteps = (int)(missingStartDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());
            int missingEndSteps = (int)(missingEndDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());

            chartCtx.layer.onDrawQuotes(quotes, missingStartSteps, missingEndSteps, chartCtx.data);

            vh.price.setText(PriceMetric.valueToString(finalQuote.close));
            if (finalQuote.close > initialQuote.open)
                vh.price.setTextColor(ContextCompat.getColor(this, R.color.colorPriceUp));
            else
                vh.price.setTextColor(ContextCompat.getColor(this, R.color.colorPriceDown));
            vh.volume.setText(VolumeMetric.valueToString(finalQuote.volume));
            vh.chart.setData(chartCtx.data);
            vh.chart.notifyDataSetChanged();
            vh.chart.fitScreen();

            addToPortfolioChart(quotes);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        int newWatchListGen = ApplicationContext.getWatchListGeneration();
        if (mWachListGeneration != newWatchListGen)
        {
            boolean itemsRemoved = false;
            List<Stock> newWatchList = ApplicationContext.getWatchList();
            for (int i =0; i < mWatchListItems.size(); ++i) {
                if (i >= newWatchList.size() || !mWatchListItems.get(i).equals(newWatchList.get(i)))
                {
                    mWatchListItems.remove(i);
                    mWatchListChartContexts.remove(i);
                    mWatchListAdapter.notifyItemRemoved(i);
                    itemsRemoved = true;
                }
            }

            if (itemsRemoved)
                mPortfolioChartContext.quotes.clear();

            for (int i = mWatchListItems.size(); i < newWatchList.size(); ++i) {
                Stock stock = newWatchList.get(i);
                mWatchListItems.add(stock);
                mWatchListChartContexts.add(new ChartContext(null));
                mWatchListAdapter.notifyItemRangeInserted(mWatchListItems.size() -1, 1);
                lanchTaskForStock(stock);
            }
        }

        List<Stock> stocksChosen = Stock.loadFrom(data);
        if (stocksChosen != null)
        {
            Intent intent = new Intent(this, QuotesActivity.class);
            Stock.saveTo(intent, stocksChosen);
            startActivity(intent);
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
        startActivity(intent);
    }

    private void lanchTaskForStock(Stock stock)
    {
        QuoteQuery query = new QuoteQuery(stock.getSymbol(), QuoteQuery.Period.OneDay, QuoteQuery.Interval.FiveMinutes);
        queriesInFlight.put(query, stock);

        QuoteQueryTask task = new QuoteQueryTask(this, ApplicationContext.getQuoteService(), this);
        task.execute(query);
    }

    private void addToPortfolioChart(List<Quote> quotes)
    {
        LineData ld = mPortfolioChartContext.data.getLineData();
        if (ld != null)
            ld.clearValues();

        if (mPortfolioChartContext.quotes.size() == 0)
        {
            for (Quote quote : quotes)
            {
                Quote mergedQuote = new Quote(quote.time, quote.open, quote.close, quote.low, quote.high, quote.volume);
                mPortfolioChartContext.quotes.add(mergedQuote);
            }
        }
        else
        {
            int i;
            for (i = 0;  i < quotes.size() && i < mPortfolioChartContext.quotes.size(); ++i)
            {
                Quote incomingQuote = quotes.get(i);
                Quote mergedQuote = mPortfolioChartContext.quotes.get(i);
                mergedQuote.open += incomingQuote.open;
                mergedQuote.close += incomingQuote.close;
                mergedQuote.high += incomingQuote.high;
                mergedQuote.low += incomingQuote.low;
                mergedQuote.volume += incomingQuote.volume;
            }

            while (i < mPortfolioChartContext.quotes.size())
                mPortfolioChartContext.quotes.remove(i);
        }

        mPortfolioChartPrice.setText(PriceMetric.valueToString(mPortfolioChartContext.quotes.get(mPortfolioChartContext.quotes.size() -1).close));

        mPortfolioChartContext.layer.onDrawQuotes(mPortfolioChartContext.quotes, 0, 0, mPortfolioChartContext.data); // TODO: fix missing start / missing end steps
        mPortfolioChart.setData(mPortfolioChartContext.data);
        mPortfolioChart.notifyDataSetChanged();
        mPortfolioChart.fitScreen();
    }
}

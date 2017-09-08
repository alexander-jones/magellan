package com.magellan.magellan;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.magellan.magellan.metric.ILineDataSetStyler;
import com.magellan.magellan.metric.MetricLayerButtonAdapter;
import com.magellan.magellan.metric.price.PriceLineLayer;
import com.magellan.magellan.metric.price.PriceMetric;
import com.magellan.magellan.quote.IQuoteQueryListener;
import com.magellan.magellan.quote.Quote;
import com.magellan.magellan.quote.QuoteQuery;
import com.magellan.magellan.quote.QuoteQueryTask;
import com.magellan.magellan.equity.Equity;

import org.joda.time.Duration;

import java.util.ArrayList;
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
    private int mComparisonGeneration;
    private int mLastMissingStartSteps;
    private int mLastMissingEndSteps;

    private DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    private RecyclerView mWatchListContainer;
    private WatchlistStockAdapter mWatchListAdapter;
    private List<Equity> mWatchListItems = new ArrayList<Equity>();
    private List<WatchListStockContext> mWatchListWatchListStockContexts = new ArrayList<WatchListStockContext>();
    private HashMap<QuoteQuery, Equity> stockQueriesInFlight = new HashMap<QuoteQuery, Equity>();
    private HashMap<QuoteQuery, Equity> indexQueriesInFlight = new HashMap<QuoteQuery, Equity>();

    private List<Integer> mComparisonColors = new ArrayList<Integer>();
    private List<Equity> mComparisones = new ArrayList<Equity>();
    private List<String> mComparisonItemLabels = new ArrayList<String>();
    private List<IndexContext> mComparisonContexts = new ArrayList<IndexContext>();

    private CombinedChart mComparisonChart;
    private CombinedData mComparisonData = new CombinedData();
    private LinearLayout.LayoutParams mComparisonPriceLayoutParams;
    private LinearLayout mComparisonPriceContainer;
    private RecyclerView mComparisonItemButtonContainer;
    private MetricLayerButtonAdapter mComparisonItemButtonAdapter;
    private ImageButton mEditLayersButton;

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

        mComparisonPriceLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mComparisonPriceLayoutParams.setMargins(0, (int)getResources().getDimension(R.dimen.spacing_internal), (int)getResources().getDimension(R.dimen.spacing_external),0);

        mComparisones = new ArrayList<Equity>(ApplicationContext.getComparisonEquities());
        mComparisonColors = new ArrayList<Integer>(ApplicationContext.getComparisonEquityColors());
        for (int i =0; i < mComparisones.size(); ++i)
            mComparisonItemLabels.add(mComparisones.get(i).getSymbol());

        mComparisonGeneration = ApplicationContext.getComparisonEquityGeneration();
        mEditLayersButton = (ImageButton) findViewById(R.id.edit_layers);
        mEditLayersButton.setOnClickListener(this);
        mComparisonPriceContainer = (LinearLayout) findViewById(R.id.price_container);
        mComparisonItemButtonContainer = (RecyclerView) findViewById(R.id.comparison_layers);
        mComparisonItemButtonAdapter = new MetricLayerButtonAdapter(mComparisonItemLabels, mComparisonColors);
        mComparisonItemButtonContainer.setAdapter(mComparisonItemButtonAdapter);

        mComparisonChart = (CombinedChart)findViewById(R.id.comparison_chart);
        for (int i =0; i < mComparisones.size(); ++i)
        {
            Equity index = mComparisones.get(i);
            TextView textView = createPriceTextView(mComparisonColors.get(i));
            mComparisonPriceContainer.addView(textView, mComparisonPriceLayoutParams);
            mComparisonContexts.add(new IndexContext(textView, mComparisonColors.get(i)));
            QuoteQuery query = new QuoteQuery(index.getSymbol(), QuoteQuery.Period.OneDay, QuoteQuery.Interval.FiveMinutes);
            indexQueriesInFlight.put(query, index);
            QuoteQueryTask task = new QuoteQueryTask(this, ApplicationContext.getQuoteService(), this);
            task.execute(query);
        }

        ApplicationContext.initializeSimpleChart(this, mComparisonChart);

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

        for (Equity equity : mWatchListItems)
            lanchTaskForStock(equity);
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

            mLastMissingStartSteps = missingStartSteps;
            mLastMissingEndSteps = missingEndSteps;
            Equity equity = indexQueriesInFlight.remove(query);
            if (equity == null)
            {
                equity = stockQueriesInFlight.remove(query);
                int position = mWatchListItems.indexOf(equity);
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
                int position = mComparisones.indexOf(equity);
                IndexContext chartCtx = mComparisonContexts.get(position);

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
                chartCtx.layer.onDrawQuotes(chartCtx.normalized_quotes, missingStartSteps, missingEndSteps, mComparisonData);

                mComparisonChart.setData(mComparisonData);
                mComparisonChart.notifyDataSetChanged();
                mComparisonChart.fitScreen();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        int newWatchListGen = ApplicationContext.getWatchListGeneration();
        if (mWachListGeneration != newWatchListGen)
        {
            List<Equity> newWatchList = ApplicationContext.getWatchList();
            for (int i =0; i < mWatchListItems.size(); ++i) {
                if (i >= newWatchList.size() || !mWatchListItems.get(i).equals(newWatchList.get(i)))
                {
                    mWatchListItems.remove(i);
                    mWatchListWatchListStockContexts.remove(i);
                    mWatchListAdapter.notifyItemRemoved(i);
                }
            }

            for (int i = mWatchListItems.size(); i < newWatchList.size(); ++i) {
                Equity equity = newWatchList.get(i);
                mWatchListItems.add(equity);
                mWatchListWatchListStockContexts.add(new WatchListStockContext());
                mWatchListAdapter.notifyItemRangeInserted(mWatchListItems.size() -1, 1);
                lanchTaskForStock(equity);
            }
        }

        List<Equity> stocksChosen = Equity.loadFrom(data);
        if (stocksChosen != null)
        {
            Intent intent = new Intent(this, QuotesActivity.class);
            Equity.saveTo(intent, stocksChosen);
            startActivityForResult(intent, 1);
        }

        int newComparisonGeneration = ApplicationContext.getWatchListGeneration();
        if (mComparisonGeneration != newComparisonGeneration)
        {
            List<Equity> equities = ApplicationContext.getComparisonEquities();
            List<Integer> colors = ApplicationContext.getComparisonEquityColors();
            HashMap<Integer, IndexContext> commonContexts = new HashMap<Integer, IndexContext>();

            for (int i = 0; i < mComparisones.size(); )
            {
                Equity equity = mComparisones.get(i);

                int newIndex = -1;
                for (int j = 0; j < equities.size(); ++j)
                {
                    if (equities.get(j).equals(equity))
                    {
                        newIndex = j;
                        break;
                    }
                }

                if (newIndex == -1)
                {
                    mComparisonPriceContainer.removeViewAt(i);
                    mComparisones.remove(i);
                }
                else
                {
                    commonContexts.put(newIndex, mComparisonContexts.get(i));
                    ++i;
                }
            }

            mComparisonItemLabels.clear();
            mComparisonContexts.clear();
            mComparisones.clear();
            mComparisones.addAll(equities);
            mComparisonColors.clear();
            mComparisonColors.addAll(colors);

            LineData ld = mComparisonData.getLineData();
            if (ld != null)
                ld.clearValues();
            for (int i = 0; i < mComparisones.size(); ++i)
            {
                Equity equity = mComparisones.get(i);
                IndexContext existingContext = commonContexts.get(i);
                mComparisonItemLabels.add(equity.getSymbol());
                if (existingContext == null)
                {
                    int color = mComparisonColors.get(i);
                    TextView textView = createPriceTextView(color);
                    mComparisonPriceContainer.addView(textView, i, mComparisonPriceLayoutParams);
                    mComparisonContexts.add(new IndexContext(textView, color));
                    QuoteQuery query = new QuoteQuery(equity.getSymbol(), QuoteQuery.Period.OneDay, QuoteQuery.Interval.FiveMinutes);
                    indexQueriesInFlight.put(query, equity);
                    QuoteQueryTask task = new QuoteQueryTask(this, ApplicationContext.getQuoteService(), this);
                    task.execute(query);
                }
                else
                {
                    existingContext.layer.setStyler(new LineDataSetStyler(mComparisonColors.get(i)));
                    mComparisonContexts.add(existingContext);
                    existingContext.layer.onDrawQuotes(existingContext.normalized_quotes, mLastMissingStartSteps, mLastMissingEndSteps, mComparisonData);
                }
            }
            mComparisonItemButtonAdapter.notifyDataSetChanged();
            mComparisonChart.setData(mComparisonData);
            mComparisonChart.notifyDataSetChanged();
            mComparisonChart.fitScreen();
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
                Intent intent = new Intent(this, EquityQueryActivity.class);
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
        if (view == mEditLayersButton)
        {
            Intent intent = new Intent(this, EditComparisonEquitiesActivity.class);
            startActivityForResult(intent, 1);
        }
        else
        {
            Intent intent = new Intent(this, QuotesActivity.class);
            intent.putExtra("WATCHLIST_ITEM", mWatchListItems.indexOf(view.getTag()));
            startActivityForResult(intent, 1);
        }
    }

    private void lanchTaskForStock(Equity equity)
    {
        QuoteQuery query = new QuoteQuery(equity.getSymbol(), QuoteQuery.Period.OneDay, QuoteQuery.Interval.FiveMinutes);
        stockQueriesInFlight.put(query, equity);

        QuoteQueryTask task = new QuoteQueryTask(this, ApplicationContext.getQuoteService(), this);
        task.execute(query);
    }
}

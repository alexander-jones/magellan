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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.magellan.magellan.metric.CenterLineLayer;
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

    private int mWachListGeneration;
    private RecyclerView mWatchListContainer;
    private WatchListRowAdapter mWatchListAdapter;
    private WatchList mWatchList;
    private List<WatchListRowAdapter.DataHolder> mWatchListItems = new ArrayList<WatchListRowAdapter.DataHolder>();
    private HashMap<QuoteQuery, WatchListRowAdapter.DataHolder> watchListQueriesInFlight = new HashMap<QuoteQuery, WatchListRowAdapter.DataHolder>();
    private HashMap<QuoteQuery, Equity> comparisonQueriesInFlight = new HashMap<QuoteQuery, Equity>();

    private List<Integer> mComparisonColors = new ArrayList<Integer>();
    private List<Equity> mComparisonEquities = new ArrayList<Equity>();
    private List<String> mComparisonButtonLabels = new ArrayList<String>();
    private List<String> mComparisonValueLabels = new ArrayList<String>();
    private List<ComparisonLayerHolder> mComparisonLayerHolders = new ArrayList<ComparisonLayerHolder>();

    private boolean mComparisonCenterLineDrawn = false;
    private CenterLineLayer mComparisonCenterLineLayer;
    private int           mComparisonDisabledColor;
    private CombinedChart mComparisonChart;
    private CombinedData mComparisonData = new CombinedData();
    private LinearLayout.LayoutParams mComparisonPriceLayoutParams;
    private RecyclerView mComparisonItemButtonContainer;
    private PortfolioComparisonItemAdapter mComparisonItemButtonAdapter;
    private ImageButton mComparisonEditLayersButton;
    private int mComparisonGeneration;
    private int mLastMissingStartSteps;
    private int mLastMissingEndSteps;
    private float mLowestComparisonValue = Float.MAX_VALUE;
    private float mHighestComparisonValue = Float.MIN_VALUE;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private class ComparisonLayerHolder
    {
        public ComparisonLayerHolder(int c)
        {
            layer = new PriceLineLayer(new SolidLineDataSetStyler(c));
            original_quotes = null;
            percent_difference_quotes = null;
            enabled = true;
            color = c;
        }

        PriceLineLayer layer;
        List<Quote> original_quotes;
        List<Quote> percent_difference_quotes;
        boolean enabled;
        int color;
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
        mComparisonPriceLayoutParams.setMargins(0, 0, (int)getResources().getDimension(R.dimen.spacing_external),0);

        mComparisonDisabledColor = ContextCompat.getColor(PortfolioActivity.this, R.color.colorCardBackgroundDark);
        mComparisonEquities = new ArrayList<Equity>(ApplicationContext.getComparisonEquities());
        mComparisonColors = new ArrayList<Integer>(ApplicationContext.getComparisonEquityColors());
        for (int i =0; i < mComparisonEquities.size(); ++i)
        {
            mComparisonValueLabels.add("0.0%");
            mComparisonButtonLabels.add(mComparisonEquities.get(i).getSymbol());
        }

        mComparisonGeneration = ApplicationContext.getComparisonEquityGeneration();
        mComparisonEditLayersButton = (ImageButton) findViewById(R.id.edit_layers);
        mComparisonEditLayersButton.setOnClickListener(this);
        mComparisonItemButtonContainer = (RecyclerView) findViewById(R.id.comparison_layers);
        mComparisonItemButtonAdapter = new PortfolioComparisonItemAdapter(mComparisonButtonLabels, mComparisonValueLabels, mComparisonColors);
        mComparisonItemButtonContainer.setAdapter(mComparisonItemButtonAdapter);

        mComparisonChart = (CombinedChart)findViewById(R.id.comparison_chart);
        for (int i =0; i < mComparisonEquities.size(); ++i)
        {
            Equity index = mComparisonEquities.get(i);
            mComparisonLayerHolders.add(new ComparisonLayerHolder(mComparisonColors.get(i)));
            QuoteQuery query = new QuoteQuery(index.getSymbol(), QuoteQuery.Period.OneDay, QuoteQuery.Interval.FiveMinutes);
            comparisonQueriesInFlight.put(query, index);
            QuoteQueryTask task = new QuoteQueryTask(this, ApplicationContext.getQuoteService(), this);
            task.execute(query);
        }

        ApplicationContext.initializeSimpleChart(this, mComparisonChart);

        mComparisonCenterLineLayer = new CenterLineLayer(this);
        mComparisonCenterLineLayer.setCenter(0.0f);

        mWatchList = WatchList.getOrCreate(0);
        mWatchList.load();
        List<Equity> watchList = mWatchList.getItems();
        for (Equity e : watchList)
            mWatchListItems.add(new WatchListRowAdapter.DataHolder(e));
        mWachListGeneration = mWatchList.getGeneration();
        mWatchListContainer = (RecyclerView)findViewById(R.id.watchlist_container);
        mWatchListAdapter = new WatchListRowAdapter(this, mWatchListItems, this);
        mWatchListContainer.setAdapter(mWatchListAdapter);

        onLoadInstanceState(savedInstanceState);
        for (WatchListRowAdapter.DataHolder dh : mWatchListItems)
            launchWatchlistTask(dh);

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
                        mWatchListAdapter.notifyItemMoved(fromPos, toPos);
                        return true;
                    }
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        mWatchList.remove(position);
                        mWatchListItems.remove(position);
                        mWatchListAdapter.notifyItemRemoved(position);
                    }

                    @Override
                    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                        super.clearView(recyclerView, viewHolder);

                        if(dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                            mWatchList.move(dragFrom, dragTo);
                        }

                        dragFrom = dragTo = -1;
                    }
                });

        ith.attachToRecyclerView(mWatchListContainer);
    }

    public void onLoadInstanceState(Bundle inState)
    {
        if (inState == null)
            return;

        int disabledIndex = 0;
        int associatedIndex = -1;
        do
        {
            associatedIndex = inState.getInt("DISABLED_" + Integer.toString(disabledIndex), -1);
            if (associatedIndex != -1) {
                mComparisonLayerHolders.get(associatedIndex).enabled = false;
                mComparisonColors.set(associatedIndex, mComparisonDisabledColor);
            }
            else
                break;
            ++disabledIndex;
        }
        while (true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        int disabledIndex = 0;
        for (int i =0; i < mComparisonLayerHolders.size(); ++i)
        {
            ComparisonLayerHolder lh = mComparisonLayerHolders.get(i);
            if (!lh.enabled) {
                outState.putInt("DISABLED_" + Integer.toString(disabledIndex), i);
                ++disabledIndex;
            }
        }
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
            Equity equity = comparisonQueriesInFlight.remove(query);
            if (equity == null)
            {
                WatchListRowAdapter.DataHolder dh = watchListQueriesInFlight.remove(query);
                dh.quotes = quotes;

                int position = mWatchListItems.indexOf(dh);
                mWatchListAdapter.notifyItemChanged(position);
            }
            else
            {
                Duration intervalDuration = query.getIntervalAsDuration();

                Quote initialQuote = quotes.get(0);
                Quote finalQuote = quotes.get(quotes.size() -1);

                Duration missingStartDuration = new Duration(query.start, initialQuote.time);
                Duration missingEndDuration = new Duration(finalQuote.time, query.end);

                int missingStartSteps = (int)(missingStartDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());
                int missingEndSteps = (int)(missingEndDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());

                float startingOpen = initialQuote.open;
                mLastMissingStartSteps = missingStartSteps;
                mLastMissingEndSteps = missingEndSteps;

                int position = mComparisonEquities.indexOf(equity);
                ComparisonLayerHolder lh = mComparisonLayerHolders.get(position);
                lh.percent_difference_quotes = new ArrayList<Quote>(quotes.size());
                lh.original_quotes = quotes;
                for (Quote q : quotes)
                {
                    float open = (q.open / startingOpen) - 1.0f;
                    float close =  (q.close / startingOpen) - 1.0f;
                    float low =(q.low / startingOpen) - 1.0f;
                    float high =  (q.high / startingOpen) - 1.0f;

                    if (low < mLowestComparisonValue)
                        mLowestComparisonValue = low;
                    if (high > mHighestComparisonValue)
                        mHighestComparisonValue = high;
                    lh.percent_difference_quotes.add(new Quote(q.time, open, close, low, high, q.volume));
                }

                float finalPercentageDifference = lh.percent_difference_quotes.get(lh.percent_difference_quotes.size() -1).close;
                mComparisonValueLabels.set(position, String.format("%.2f%%", finalPercentageDifference * 100));
                mComparisonItemButtonAdapter.notifyItemChanged(position);

                if (!mComparisonCenterLineDrawn) {
                    mComparisonCenterLineLayer.onDrawQuotes(quotes, 0, 0, mComparisonData);
                    mComparisonCenterLineDrawn = true;
                }
                if (lh.enabled)
                    lh.layer.onDrawQuotes(lh.percent_difference_quotes, missingStartSteps, missingEndSteps, mComparisonData);


                /*float absDiffFromHigh =  Math.abs(mHighestComparisonValue);
                float diffFromCenter = Math.abs(mLowestComparisonValue);
                if (diffFromCenter < absDiffFromHigh)
                    diffFromCenter = absDiffFromHigh;*/

                YAxis axis = mComparisonChart.getAxisLeft();
                axis.setAxisMinimum(mLowestComparisonValue);
                axis.setAxisMaximum(mHighestComparisonValue);
                mComparisonChart.setData(mComparisonData);
                mComparisonChart.notifyDataSetChanged();
                mComparisonChart.fitScreen();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        WatchList newWatchList = WatchList.get(0);
        newWatchList.load();
        int newWatchListGeneration = newWatchList.getGeneration();
        if (mWachListGeneration != newWatchListGeneration)
        {
            mWachListGeneration = newWatchListGeneration;
            List<Equity> newWatchListItems = newWatchList.getItems();
            for (int i =0; i < mWatchListItems.size(); ++i) {
                if (i >= newWatchListItems.size() || !mWatchListItems.get(i).equity.equals(newWatchListItems.get(i)))
                {
                    mWatchListItems.remove(i);
                    mWatchListAdapter.notifyItemRemoved(i);
                }
            }

            for (int i = mWatchListItems.size(); i < newWatchListItems.size(); ++i) {
                Equity equity = newWatchListItems.get(i);
                WatchListRowAdapter.DataHolder dh = new WatchListRowAdapter.DataHolder(equity);
                mWatchListItems.add(dh);
                mWatchListAdapter.notifyItemRangeInserted(mWatchListItems.size() -1, 1);
                launchWatchlistTask(dh);
            }
        }

        List<Equity> stocksChosen = Equity.loadFrom(data);
        if (stocksChosen != null)
        {
            Intent intent = new Intent(this, QuotesActivity.class);
            Equity.saveTo(intent, stocksChosen);
            startActivityForResult(intent, 1);
        }

        int newComparisonGeneration = ApplicationContext.getComparisonEquityGeneration();
        if (mComparisonGeneration != newComparisonGeneration)
        {
            mComparisonGeneration = newComparisonGeneration;
            List<Equity> equities = ApplicationContext.getComparisonEquities();
            List<Integer> colors = ApplicationContext.getComparisonEquityColors();
            HashMap<Integer, ComparisonLayerHolder> commonContexts = new HashMap<Integer, ComparisonLayerHolder>();

            for (int i = 0; i < mComparisonEquities.size(); )
            {
                Equity equity = mComparisonEquities.get(i);

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
                    mComparisonValueLabels.remove(i);
                    mComparisonEquities.remove(i); // this is only neccessary so layer lookup doesn't disconnect
                }
                else
                {
                    commonContexts.put(newIndex, mComparisonLayerHolders.get(i));
                    ++i;
                }
            }

            mComparisonButtonLabels.clear();
            mComparisonLayerHolders.clear();
            mComparisonEquities.clear();
            mComparisonEquities.addAll(equities);
            mComparisonColors.clear();
            mComparisonColors.addAll(colors);

            LineData ld = mComparisonData.getLineData();
            if (ld != null) {
                ld.clearValues();
                mComparisonCenterLineDrawn = false;
            }
            for (int i = 0; i < mComparisonEquities.size(); ++i)
            {
                Equity equity = mComparisonEquities.get(i);
                ComparisonLayerHolder existingContext = commonContexts.get(i);
                mComparisonButtonLabels.add(equity.getSymbol());
                if (existingContext == null)
                {
                    int color = mComparisonColors.get(i);
                    mComparisonValueLabels.add("0.00%");
                    mComparisonLayerHolders.add(new ComparisonLayerHolder(color));
                    QuoteQuery query = new QuoteQuery(equity.getSymbol(), QuoteQuery.Period.OneDay, QuoteQuery.Interval.FiveMinutes);
                    comparisonQueriesInFlight.put(query, equity);
                    QuoteQueryTask task = new QuoteQueryTask(this, ApplicationContext.getQuoteService(), this);
                    task.execute(query);
                }
                else
                {
                    existingContext.layer.setStyler(new SolidLineDataSetStyler(mComparisonColors.get(i)));
                    mComparisonLayerHolders.add(existingContext);
                    if (existingContext.enabled)
                        existingContext.layer.onDrawQuotes(existingContext.percent_difference_quotes, mLastMissingStartSteps, mLastMissingEndSteps, mComparisonData);
                    else
                        mComparisonColors.set(i, mComparisonDisabledColor);
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
        if (view == mComparisonEditLayersButton)
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

    private void launchWatchlistTask(WatchListRowAdapter.DataHolder dh)
    {
        dh.query = new QuoteQuery(dh.equity.getSymbol(), QuoteQuery.Period.OneDay, QuoteQuery.Interval.FiveMinutes);
        watchListQueriesInFlight.put(dh.query, dh);

        QuoteQueryTask task = new QuoteQueryTask(this, ApplicationContext.getQuoteService(), this);
        task.execute(dh.query);
    }

    private void redrawComparisonChart()
    {
        mLowestComparisonValue = Float.MAX_VALUE;
        mHighestComparisonValue = Float.MIN_VALUE;
        mComparisonCenterLineDrawn = false;
        LineData ld = mComparisonData.getLineData();
        if (ld != null) {
            ld.clearValues();
        }

        for (int i = 0; i < mComparisonEquities.size(); ++i)
        {
            ComparisonLayerHolder lh = mComparisonLayerHolders.get(i);
            if (lh.enabled) {
                float startingOpen = lh.percent_difference_quotes.get(0).open;
                for (Quote q : lh.percent_difference_quotes) {
                    if (q.low < mLowestComparisonValue)
                        mLowestComparisonValue = q.low;
                    if (q.high > mHighestComparisonValue)
                        mHighestComparisonValue = q.high;
                }

                if (!mComparisonCenterLineDrawn)
                {
                    mComparisonCenterLineLayer.onDrawQuotes(lh.percent_difference_quotes, 0, 0, mComparisonData);
                    mComparisonCenterLineDrawn = true;
                }

                lh.layer.onDrawQuotes(lh.percent_difference_quotes, mLastMissingStartSteps, mLastMissingEndSteps, mComparisonData);
            }
        }

        /*float absDiffFromHigh =  Math.abs(mHighestComparisonValue);
        float diffFromCenter = Math.abs(mLowestComparisonValue);
        if (diffFromCenter < absDiffFromHigh)
            diffFromCenter = absDiffFromHigh;*/

        mComparisonChart.setData(mComparisonData);
        YAxis axis = mComparisonChart.getAxisLeft();
        axis.setAxisMinimum(mLowestComparisonValue);
        axis.setAxisMaximum(mHighestComparisonValue);
        mComparisonChart.notifyDataSetChanged();
        mComparisonChart.fitScreen();
    }

    private class PortfolioComparisonItemAdapter extends ComparisonItemAdapter
    {
        PortfolioComparisonItemAdapter(List<String> labels, List<String> values, List<Integer> colors)
        {
            super(labels, values, colors);
        }

        @Override
        public void onButtonPressed(TextView button)
        {
            CharSequence symbol = button.getText();
            int index = -1;
            for (int i =0 ; i < mComparisonEquities.size(); ++i)
            {
                if (mComparisonEquities.get(i).getSymbol().equals(symbol))
                {
                    index = i;
                    break;
                }
            }

            ComparisonLayerHolder lh = mComparisonLayerHolders.get(index);
            if (lh.enabled)
            {
                mComparisonColors.set(index, mComparisonDisabledColor);
                lh.enabled = false;
            }
            else
            {
                mComparisonColors.set(index, lh.color);
                lh.enabled = true;
            }

            notifyItemChanged(index);
            redrawComparisonChart();
        }
    }
}

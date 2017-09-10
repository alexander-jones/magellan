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
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.magellan.magellan.metric.price.PriceLineLayer;
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
    private List<WatchListRowAdapter.DataHolder> mWatchListItems = new ArrayList<WatchListRowAdapter.DataHolder>();
    private HashMap<QuoteQuery, WatchListRowAdapter.DataHolder> watchListQueriesInFlight = new HashMap<QuoteQuery, WatchListRowAdapter.DataHolder>();
    private HashMap<QuoteQuery, Equity> comparisonQueriesInFlight = new HashMap<QuoteQuery, Equity>();

    private List<Integer> mComparisonColors = new ArrayList<Integer>();
    private List<Equity> mComparisonEquities = new ArrayList<Equity>();
    private List<String> mComparisonItemLabels = new ArrayList<String>();
    private List<ComparisonLayerHolder> mComparisonLayerHolders = new ArrayList<ComparisonLayerHolder>();

    private int           mComparisonDisabledColor;
    private CombinedChart mComparisonChart;
    private CombinedData mComparisonData = new CombinedData();
    private LinearLayout.LayoutParams mComparisonPriceLayoutParams;
    private LinearLayout mComparisonPriceContainer;
    private RecyclerView mComparisonItemButtonContainer;
    private ButtonAdapter mComparisonItemButtonAdapter;
    private ImageButton mComparisonEditLayersButton;
    private int mComparisonGeneration;
    private int mLastMissingStartSteps;
    private int mLastMissingEndSteps;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private class ComparisonLayerHolder
    {
        public ComparisonLayerHolder(TextView priceTextView, int c)
        {
            price = priceTextView;
            layer = new PriceLineLayer(new SolidLineDataSetStyler(c));
            original_quotes = null;
            percent_difference_quotes = null;
            enabled = true;
            color = c;
        }

        TextView price;
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

        mComparisonDisabledColor = ContextCompat.getColor(PortfolioActivity.this, R.color.colorSecondaryDark);
        mComparisonEquities = new ArrayList<Equity>(ApplicationContext.getComparisonEquities());
        mComparisonColors = new ArrayList<Integer>(ApplicationContext.getComparisonEquityColors());
        for (int i =0; i < mComparisonEquities.size(); ++i)
            mComparisonItemLabels.add(mComparisonEquities.get(i).getSymbol());

        mComparisonGeneration = ApplicationContext.getComparisonEquityGeneration();
        mComparisonEditLayersButton = (ImageButton) findViewById(R.id.edit_layers);
        mComparisonEditLayersButton.setOnClickListener(this);
        mComparisonPriceContainer = (LinearLayout) findViewById(R.id.price_container);
        mComparisonItemButtonContainer = (RecyclerView) findViewById(R.id.comparison_layers);
        mComparisonItemButtonAdapter = new ComparisonButtonAdapter(mComparisonItemLabels, mComparisonColors);
        mComparisonItemButtonContainer.setAdapter(mComparisonItemButtonAdapter);

        mComparisonChart = (CombinedChart)findViewById(R.id.comparison_chart);
        for (int i =0; i < mComparisonEquities.size(); ++i)
        {
            Equity index = mComparisonEquities.get(i);
            TextView textView = createPriceTextView(mComparisonColors.get(i));
            mComparisonPriceContainer.addView(textView, mComparisonPriceLayoutParams);
            mComparisonLayerHolders.add(new ComparisonLayerHolder(textView, mComparisonColors.get(i)));
            QuoteQuery query = new QuoteQuery(index.getSymbol(), QuoteQuery.Period.OneDay, QuoteQuery.Interval.FiveMinutes);
            comparisonQueriesInFlight.put(query, index);
            QuoteQueryTask task = new QuoteQueryTask(this, ApplicationContext.getQuoteService(), this);
            task.execute(query);
        }

        ApplicationContext.initializeSimpleChart(this, mComparisonChart);

        List<Equity> watchList = ApplicationContext.getWatchList();
        for (Equity e : watchList)
            mWatchListItems.add(new WatchListRowAdapter.DataHolder(e));
        mWachListGeneration = ApplicationContext.getWatchListGeneration();
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
                        ApplicationContext.removeFromWatchList(position);
                        mWatchListItems.remove(position);
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
                    lh.percent_difference_quotes.add(new Quote(q.time, open, close, low, high, q.volume));
                }

                float finalPercentageDifference = lh.percent_difference_quotes.get(lh.percent_difference_quotes.size() -1).close;
                lh.price.setText(String.format("%.2f%%", finalPercentageDifference * 100));
                if (lh.enabled)
                    lh.layer.onDrawQuotes(lh.percent_difference_quotes, missingStartSteps, missingEndSteps, mComparisonData);

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
                if (i >= newWatchList.size() || !mWatchListItems.get(i).equity.equals(newWatchList.get(i)))
                {
                    mWatchListItems.remove(i);
                    mWatchListAdapter.notifyItemRemoved(i);
                }
            }

            for (int i = mWatchListItems.size(); i < newWatchList.size(); ++i) {
                Equity equity = newWatchList.get(i);
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
                    mComparisonPriceContainer.removeViewAt(i);
                    mComparisonEquities.remove(i); // this is only neccessary so layer lookup doesn't disconnect
                }
                else
                {
                    commonContexts.put(newIndex, mComparisonLayerHolders.get(i));
                    ++i;
                }
            }

            mComparisonItemLabels.clear();
            mComparisonLayerHolders.clear();
            mComparisonEquities.clear();
            mComparisonEquities.addAll(equities);
            mComparisonColors.clear();
            mComparisonColors.addAll(colors);

            LineData ld = mComparisonData.getLineData();
            if (ld != null)
                ld.clearValues();
            for (int i = 0; i < mComparisonEquities.size(); ++i)
            {
                Equity equity = mComparisonEquities.get(i);
                ComparisonLayerHolder existingContext = commonContexts.get(i);
                mComparisonItemLabels.add(equity.getSymbol());
                if (existingContext == null)
                {
                    int color = mComparisonColors.get(i);
                    TextView textView = createPriceTextView(color);
                    mComparisonPriceContainer.addView(textView, i, mComparisonPriceLayoutParams);
                    mComparisonLayerHolders.add(new ComparisonLayerHolder(textView, color));
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
        LineData ld = mComparisonData.getLineData();
        if (ld != null)
            ld.clearValues();
        for (int i = 0; i < mComparisonEquities.size(); ++i)
        {
            ComparisonLayerHolder lh = mComparisonLayerHolders.get(i);
            if (lh.enabled)
                lh.layer.onDrawQuotes(lh.percent_difference_quotes, mLastMissingStartSteps, mLastMissingEndSteps, mComparisonData);
        }
        mComparisonItemButtonAdapter.notifyDataSetChanged();
        mComparisonChart.setData(mComparisonData);
        mComparisonChart.notifyDataSetChanged();
        mComparisonChart.fitScreen();
    }

    private class ComparisonButtonAdapter extends ButtonAdapter
    {
        ComparisonButtonAdapter(List<String> labels, List<Integer> colors)
        {
            super(labels, colors);
        }

        @Override
        public void onButtonPressed(Button button)
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

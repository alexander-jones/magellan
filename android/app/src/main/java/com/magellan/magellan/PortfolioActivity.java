package com.magellan.magellan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.magellan.magellan.metric.CenterLineLayer;
import com.magellan.magellan.metric.price.PriceLineLayer;
import com.magellan.magellan.quote.IQuoteQueryListener;
import com.magellan.magellan.quote.Quote;
import com.magellan.magellan.quote.QuoteQuery;
import com.magellan.magellan.quote.QuoteQueryTask;
import com.magellan.magellan.equity.Equity;
import org.joda.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PortfolioActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private TextView mHeader;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private PortfolioList mPortfolioList;
    private PagerAdapter mAdapter;
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mHeader = (TextView)findViewById(R.id.header_value);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.portfolio_tabs);

        mPortfolioList = ApplicationContext.getPortfolios(this);
        mPortfolioList.load();
        while (mPortfolioList.size() < 2)
            mPortfolioList.addDefaultItem("Portfolio " + Integer.toString(mPortfolioList.size()));

        for (PortfolioList.Item item : mPortfolioList) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(item.name);
            tab.setTag(item);
            tabLayout.addTab(tab);
        }

        TabLayout.Tab selectedTab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());

        mPager = (ViewPager) findViewById(R.id.view_pager);
        mAdapter = new PagerAdapter();
        mPager.setAdapter(mAdapter);
        tabLayout.setupWithViewPager(mPager);
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter() {
            super(PortfolioActivity.this.getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            PortfolioFragment frag = new PortfolioFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("PORTFOLIO_INDEX", position);
            frag.setArguments(bundle);
            return frag;
        }

        @Override
        public int getCount() {
            return mPortfolioList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mPortfolioList.get(position).name;
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
                int portfolioIndex = mPager.getCurrentItem();
                PortfolioFragment frag = (PortfolioFragment)getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + portfolioIndex);
                Intent intent = new Intent(this, EquityQueryActivity.class);
                intent.putExtra("PORTFOLIO", portfolioIndex);
                frag.startActivityForResult(intent, 1);
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

    public static class PortfolioFragment extends Fragment implements View.OnClickListener, IQuoteQueryListener {

        private PortfolioList.Item mPortfolioItem;
        private int mPortfolioIndex;

        private RecyclerView mWatchListContainer;
        private WatchListRowAdapter mWatchListAdapter;
        private List<WatchListRowAdapter.DataHolder> mWatchListRowData = new ArrayList<WatchListRowAdapter.DataHolder>();
        private HashMap<QuoteQuery, WatchListRowAdapter.DataHolder> watchListQueriesInFlight = new HashMap<QuoteQuery, WatchListRowAdapter.DataHolder>();

        private int mCenterLineSteps = 0;
        private int mLastMissingStartSteps;
        private int mLastMissingEndSteps;
        private CenterLineLayer mComparisonCenterLineLayer;
        private CombinedChart mComparisonChart;
        private CombinedData mComparisonData = new CombinedData();
        private RecyclerView mComparisonItemButtonContainer;
        private PortfolioComparisonItemAdapter mComparisonItemButtonAdapter;
        private ImageButton mComparisonEditLayersButton;
        private float mLowestComparisonValue = Float.MAX_VALUE;
        private float mHighestComparisonValue = Float.MIN_VALUE;
        private int mComparisonIntervalGeneration = 0;
        private TabLayout mIntervalTabLayout;
        private List<String> mComparisonButtonLabels = new ArrayList<String>();
        private List<String> mComparisonValueLabels = new ArrayList<String>();
        private List<ComparisonLayerHolder> mComparisonLayerHolders = new ArrayList<ComparisonLayerHolder>();
        private HashMap<QuoteQuery, ComparisonQueryHolder> comparisonQueriesInFlight = new HashMap<QuoteQuery, ComparisonQueryHolder>();

        private class ComparisonQueryHolder
        {
            ComparisonQueryHolder(QuoteQuery q, QuoteQueryTask t , ComparisonList.Item i)
            {
                query = q;
                task = t;
                item = i;
            }
            QuoteQuery query;
            QuoteQueryTask task;
            ComparisonList.Item item;
        }

        private class ComparisonLayerHolder
        {
            public ComparisonLayerHolder(ComparisonList.Item i)
            {
                layer = new PriceLineLayer(new SolidLineDataSetStyler(i.color));
                original_quotes = null;
                percent_difference_quotes = null;
                item = i;
            }

            ComparisonList.Item item;
            PriceLineLayer layer;
            List<Quote> original_quotes;
            List<Quote> percent_difference_quotes;
        }

        @Override
        public void onAttach(Context context)
        {
            super.onAttach(context);
            PortfolioList portfolioList = ApplicationContext.getPortfolios(context);
            portfolioList.load();
            mPortfolioIndex = getArguments().getInt("PORTFOLIO_INDEX", -1);
            mPortfolioItem = portfolioList.get(mPortfolioIndex);
            mPortfolioItem.load();
            for (ComparisonList.Item comparisonItem : mPortfolioItem.getComparisonList())
            {
                mComparisonValueLabels.add("0.0%");
                mComparisonButtonLabels.add(comparisonItem.equity.getSymbol());
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_portfolio, null);

            mComparisonEditLayersButton = (ImageButton) rootView.findViewById(R.id.edit_layers);
            mComparisonEditLayersButton.setOnClickListener(this);

            mComparisonItemButtonContainer = (RecyclerView) rootView.findViewById(R.id.comparison_layers);
            mComparisonItemButtonContainer.requestDisallowInterceptTouchEvent(true);
            mComparisonItemButtonAdapter = new PortfolioComparisonItemAdapter(mComparisonItemButtonContainer, mPortfolioItem.getComparisonList(), mComparisonValueLabels);
            mComparisonItemButtonContainer.setAdapter(mComparisonItemButtonAdapter);

            mIntervalTabLayout = (TabLayout)rootView.findViewById(R.id.interval_tabs);

            mIntervalTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    drawSelectedInterval();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });

            mComparisonChart = (CombinedChart)rootView.findViewById(R.id.comparison_chart);
            for (ComparisonList.Item comparisonItem : mPortfolioItem.getComparisonList())
                mComparisonLayerHolders.add(new ComparisonLayerHolder(comparisonItem));

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            ViewGroup.LayoutParams params = mComparisonChart.getLayoutParams();

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                params.height = displayMetrics.heightPixels / 2;
            else
                params.height = (int)(displayMetrics.heightPixels * 0.35f);

            mComparisonChart.setLayoutParams(params);

            ApplicationContext.initializeSimpleChart(getContext(), mComparisonChart);

            mComparisonCenterLineLayer = new CenterLineLayer(getContext());
            mComparisonCenterLineLayer.setCenter(0.0f);

            for (Equity e : mPortfolioItem.getWatchList())
                mWatchListRowData.add(new WatchListRowAdapter.DataHolder(e));
            mWatchListContainer = (RecyclerView)rootView.findViewById(R.id.watchlist_container);
            mWatchListAdapter = new WatchListRowAdapter(getContext(), mWatchListRowData, this);
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
                            Collections.swap(mWatchListRowData, fromPos, toPos);
                            mWatchListAdapter.notifyItemMoved(fromPos, toPos);
                            return true;
                        }
                        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                            int position = viewHolder.getAdapterPosition();
                            mPortfolioItem.getWatchList().remove(position);
                            mWatchListRowData.remove(position);
                            mWatchListAdapter.notifyItemRemoved(position);
                        }

                        @Override
                        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                            super.clearView(recyclerView, viewHolder);

                            if(dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                                WatchList watchList = mPortfolioItem.getWatchList();
                                watchList.add(dragTo, watchList.remove(dragFrom));
                            }

                            dragFrom = dragTo = -1;
                        }
                    });

            ith.attachToRecyclerView(mWatchListContainer);

            onLoadInstanceState(savedInstanceState);
            for (WatchListRowAdapter.DataHolder dh : mWatchListRowData)
                launchWatchlistTask(dh);
            drawSelectedInterval();


            return rootView;
        }

        public void onLoadInstanceState(Bundle inState)
        {
            if (inState == null)
                return;

            int selectedTab = inState.getInt("SELECTED_INTERVAL", -1);
            if (selectedTab != -1)
                mIntervalTabLayout.getTabAt(selectedTab).select();
        }

        @Override
        public void onSaveInstanceState(Bundle outState)
        {
            outState.putInt("SELECTED_INTERVAL", mIntervalTabLayout.getSelectedTabPosition());
        }


        private void launchQueriesForComparisons(List<ComparisonList.Item> items)
        {
            int position = mIntervalTabLayout.getSelectedTabPosition();
            QuoteQuery.Period [] quotePeriods = QuoteQuery.Period.values();
            if (position <0 || position > quotePeriods.length)
            {
                Log.e("Magellan", "Encountered Unknown Duration Tab Index");
                return;
            }

            QuoteQuery.Period period = quotePeriods[position];

            QuoteQuery.Interval interval = QuoteQuery.Interval.FiveMinutes;
            switch (period)
            {
                case OneDay:
                    interval = QuoteQuery.Interval.FiveMinutes;
                    break;
                case OneWeek:
                    interval = QuoteQuery.Interval.FifteenMinutes;
                    break;
                case OneMonth:
                    interval = QuoteQuery.Interval.OneDay;
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

            for (ComparisonList.Item item: items)
            {
                QuoteQuery query = new QuoteQuery(item.equity.getSymbol(), period, interval, mComparisonIntervalGeneration);
                mCenterLineSteps = query.getExpectedSteps();
                QuoteQueryTask task = new QuoteQueryTask(getContext(), ApplicationContext.getQuoteService(), this);
                comparisonQueriesInFlight.put(query, new ComparisonQueryHolder(query, task, item));
                task.execute(query);
            }
        }

        private void drawSelectedInterval()
        {
            cleanComparisonContext();
            for (ComparisonQueryHolder holder : comparisonQueriesInFlight.values())
            {
                if (holder.task.getStatus().equals(AsyncTask.Status.RUNNING))
                    holder.task.cancel(true);
            }

            comparisonQueriesInFlight.clear();
            mComparisonIntervalGeneration++;
            launchQueriesForComparisons(mPortfolioItem.getComparisonList());
            if (mCenterLineSteps != 0)
                mComparisonCenterLineLayer.draw(mCenterLineSteps, mComparisonData);
            mComparisonChart.setData(mComparisonData);
            mComparisonChart.notifyDataSetChanged();
            mComparisonChart.fitScreen();
        }


        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            WatchList watchList = mPortfolioItem.getWatchList();
            int newWatchListGeneration = watchList.getNewestGeneration();
            if (newWatchListGeneration != watchList.getGeneration())
            {
                watchList.load();
                for (int i = 0; i < mWatchListRowData.size(); ++i) {
                    if (i >= watchList.size() || !mWatchListRowData.get(i).equity.equals(watchList.get(i)))
                    {
                        mWatchListRowData.remove(i);
                        mWatchListAdapter.notifyItemRemoved(i);
                    }
                }

                for (int i = mWatchListRowData.size(); i < watchList.size(); ++i) {
                    Equity equity = watchList.get(i);
                    WatchListRowAdapter.DataHolder dh = new WatchListRowAdapter.DataHolder(equity);
                    mWatchListRowData.add(dh);
                    mWatchListAdapter.notifyItemRangeInserted(mWatchListRowData.size() -1, 1);
                    launchWatchlistTask(dh);
                }
            }

            List<Equity> stocksChosen = Equity.loadFrom(data);
            if (stocksChosen != null)
            {
                Intent intent = new Intent(getContext(), QuotesActivity.class);
                Equity.saveTo(intent, stocksChosen);
                intent.putExtra("PORTFOLIO", mPortfolioIndex);
                startActivityForResult(intent, 1);
            }

            ComparisonList comparisonList = mPortfolioItem.getComparisonList();
            int newComparisonGeneration = comparisonList.getNewestGeneration();
            if (newComparisonGeneration != comparisonList.getGeneration())
            {
                comparisonList.load();
                HashMap<Integer, ComparisonLayerHolder> commonContexts = new HashMap<Integer, ComparisonLayerHolder>();

                for (int i = 0; i < mComparisonLayerHolders.size(); ++i)
                {
                    for (int j = 0; j < comparisonList.size(); ++j)
                    {
                        if (mComparisonLayerHolders.get(i).item.equals(comparisonList.get(j)))
                        {
                            commonContexts.put(j, mComparisonLayerHolders.get(i));
                            break;
                        }
                    }
                }

                mComparisonButtonLabels.clear();
                mComparisonLayerHolders.clear();
                mComparisonValueLabels.clear();

                LineData ld = mComparisonData.getLineData();
                if (ld != null) {
                    ld.clearValues();
                }

                List<ComparisonList.Item> itemsToLaunch = new ArrayList<ComparisonList.Item>();
                for (int i = 0; i < comparisonList.size(); ++i)
                {
                    ComparisonList.Item item = comparisonList.get(i);
                    ComparisonLayerHolder existingContext = commonContexts.get(i);
                    mComparisonButtonLabels.add(item.equity.getSymbol());
                    mComparisonValueLabels.add("0.00%");
                    if (existingContext == null)
                    {
                        mComparisonLayerHolders.add(new ComparisonLayerHolder(item));
                        itemsToLaunch.add(item);
                    }
                    else
                    {
                        existingContext.layer.setStyler(new SolidLineDataSetStyler(item.color));
                        mComparisonLayerHolders.add(existingContext);
                        if (item.enabled)
                            existingContext.layer.onDrawQuotes(existingContext.percent_difference_quotes, mLastMissingStartSteps, mLastMissingEndSteps, mComparisonData);
                    }
                }

                if (itemsToLaunch.size() > 0)
                    launchQueriesForComparisons(itemsToLaunch);

                mComparisonItemButtonAdapter.notifyDataSetChanged();
                mComparisonChart.setData(mComparisonData);
                mComparisonChart.notifyDataSetChanged();
                mComparisonChart.fitScreen();
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
                ComparisonQueryHolder holder = comparisonQueriesInFlight.remove(query);
                if (holder == null)
                {
                    WatchListRowAdapter.DataHolder dh = watchListQueriesInFlight.remove(query);
                    dh.quotes = quotes;

                    int position = mWatchListRowData.indexOf(dh);
                    mWatchListAdapter.notifyItemChanged(position);
                }
                else
                {
                    if (query.tag != mComparisonIntervalGeneration)
                        continue;

                    int position = mPortfolioItem.getComparisonList().indexOf(holder.item);
                    if (position < 0)
                        continue;

                    Duration intervalDuration = query.getIntervalAsDuration();

                    Quote initialQuote = quotes.get(0);
                    Quote finalQuote = quotes.get(quotes.size() -1);

                    Duration missingStartDuration = new Duration(query.start, initialQuote.time);
                    Duration missingEndDuration = new Duration(finalQuote.time, query.end);

                    int missingStartSteps = Math.max((int)(missingStartDuration.getStandardMinutes() / intervalDuration.getStandardMinutes()), 0);
                    int missingEndSteps = Math.max((int)(missingEndDuration.getStandardMinutes() / intervalDuration.getStandardMinutes()), 0);

                    float startingOpen = initialQuote.open;
                    mLastMissingStartSteps = missingStartSteps;
                    mLastMissingEndSteps = missingEndSteps;

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

                    if (holder.item.enabled)
                        lh.layer.onDrawQuotes(lh.percent_difference_quotes, missingStartSteps, missingEndSteps, mComparisonData);

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
        public void onClick(View view) {
            if (view == mComparisonEditLayersButton)
            {
                Intent intent = new Intent(getContext(), EditComparisonEquitiesActivity.class);
                intent.putExtra("PORTFOLIO", mPortfolioIndex);
                startActivityForResult(intent, 1);
            }
            else
            {
                Intent intent = new Intent(getContext(), QuotesActivity.class);
                intent.putExtra("WATCHLIST_ITEM", mWatchListRowData.indexOf(view.getTag()));
                intent.putExtra("PORTFOLIO", mPortfolioIndex);
                startActivityForResult(intent, 1);
            }
        }

        private void launchWatchlistTask(WatchListRowAdapter.DataHolder dh)
        {
            dh.query = new QuoteQuery(dh.equity.getSymbol(), QuoteQuery.Period.OneDay, QuoteQuery.Interval.FiveMinutes);
            watchListQueriesInFlight.put(dh.query, dh);

            QuoteQueryTask task = new QuoteQueryTask(getContext(), ApplicationContext.getQuoteService(), this);
            task.execute(dh.query);
        }

        private void cleanComparisonContext()
        {
            mLowestComparisonValue = Float.MAX_VALUE;
            mHighestComparisonValue = Float.MIN_VALUE;
            LineData ld = mComparisonData.getLineData();
            if (ld != null) {
                ld.clearValues();
            }
        }

        private void redrawComparisonChart()
        {
            cleanComparisonContext();

            ComparisonList comparisonList = mPortfolioItem.getComparisonList();
            for (int i = 0; i < mComparisonLayerHolders.size(); ++i)
            {
                ComparisonLayerHolder lh = mComparisonLayerHolders.get(i);
                ComparisonList.Item item = comparisonList.get(i);
                if (item.enabled) {
                    for (Quote q : lh.percent_difference_quotes) {
                        if (q.low < mLowestComparisonValue)
                            mLowestComparisonValue = q.low;
                        if (q.high > mHighestComparisonValue)
                            mHighestComparisonValue = q.high;
                    }

                    if (mCenterLineSteps != 0)
                        mComparisonCenterLineLayer.draw(mCenterLineSteps, mComparisonData);

                    lh.layer.onDrawQuotes(lh.percent_difference_quotes, mLastMissingStartSteps, mLastMissingEndSteps, mComparisonData);
                }
            }

            mComparisonChart.setData(mComparisonData);
            YAxis axis = mComparisonChart.getAxisLeft();
            axis.setAxisMinimum(mLowestComparisonValue);
            axis.setAxisMaximum(mHighestComparisonValue);
            mComparisonChart.notifyDataSetChanged();
            mComparisonChart.fitScreen();
        }


        private class PortfolioComparisonItemAdapter extends ComparisonItemAdapter
        {
            private List<Boolean> longPressStates;

            PortfolioComparisonItemAdapter(RecyclerView view, List<ComparisonList.Item> items, List<String> values)
            {
                super(view, items, values);
                longPressStates = new ArrayList<Boolean>(items.size());
            }

            @Override
            public void onItemSelected(ComparisonList.Item item)
            {
                ComparisonList comparisonList = mPortfolioItem.getComparisonList();
                int index = comparisonList.indexOf(item);
                if (index == -1)
                    return;

                if (item.enabled)
                    item.enabled = false;
                else
                    item.enabled = true;
                comparisonList.saveItem(index);
                notifyItemChanged(index);
                redrawComparisonChart();
            }

            @Override
            public void onItemLongPressed(ComparisonList.Item item)
            {
                ComparisonList comparisonList = mPortfolioItem.getComparisonList();
                int index = comparisonList.indexOf(item);
                if (index == -1)
                    return;

                longPressStates.clear();
                for (int i=0; i < comparisonList.size(); ++i)
                {
                    ComparisonList.Item otherItem = comparisonList.get(i);
                    longPressStates.add(otherItem.enabled);
                    if (i == index)
                        otherItem.enabled = true;
                    else
                        otherItem.enabled = false;
                }
                // DON'T CALL comparisonList.onItemChanged() here because it will result in these values getting saved, which will effect rotate
                notifyDataSetChanged();
                redrawComparisonChart();
            }

            @Override
            public void onItemLongPressReleased(ComparisonList.Item item)
            {
                ComparisonList comparisonList = mPortfolioItem.getComparisonList();
                int index = comparisonList.indexOf(item);
                if (index == -1)
                    return;

                for (int i=0; i < comparisonList.size(); ++i)
                {
                    ComparisonList.Item otherItem = comparisonList.get(i);
                    otherItem.enabled = longPressStates.get(i);
                }
                notifyDataSetChanged();
                redrawComparisonChart();
            }
        }
    }
}

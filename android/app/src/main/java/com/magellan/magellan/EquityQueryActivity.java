package com.magellan.magellan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.magellan.magellan.equity.Equity;
import com.magellan.magellan.equity.EquityQuery;
import com.magellan.magellan.equity.EquityQueryTask;
import com.magellan.magellan.equity.IEquityQueryListener;
import com.magellan.magellan.equity.IEquityService;
import com.magellan.magellan.service.yahoo.YahooService;

import java.util.ArrayList;
import java.util.List;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

public class EquityQueryActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, IEquityQueryListener, AdapterView.OnItemClickListener{

    private ListView mList;
    private EquityQueryTask mTask;
    private EquityRowAdapter mAdapter;
    private List<Equity> mCurrentEquities = new ArrayList<Equity>();
    private IEquityService mService = new YahooService();
    private List<String> mSupportedExchanges = new ArrayList<String>();
    private Toolbar mToolbar;
    private SearchView mSearchView;

    private String mSearchViewTextToSet = null;
    private boolean mRemoveSearchViewFocus = false;

    private PortfolioList.Item mPortfolio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_query);

        Intent intent = getIntent();
        int portfolioIndex = intent.getIntExtra("PORTFOLIO", -1);

        PortfolioList portfolioList = ApplicationContext.getPortfolios(this);
        portfolioList.load();
        mPortfolio = portfolioList.get(portfolioIndex);
        mPortfolio.load();

        setTitle("");
        mList =(ListView) findViewById(R.id.search_list_view);
        mAdapter = new EquityQueryRowAdapter(mCurrentEquities);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSupportedExchanges.add("NYSEArca");
        mSupportedExchanges.add("NYSE");
        mSupportedExchanges.add("NASDAQ");
        mSupportedExchanges.add("OTC BB");

        onLoadInstanceState(savedInstanceState);
    }


    public void onLoadInstanceState(Bundle inState)
    {
        if (inState == null)
            return;

        mSearchViewTextToSet = inState.getString("SEARCH_TEXT", null);
        mRemoveSearchViewFocus = !inState.getBoolean("SEARCH_FOCUS", false);

        List<Equity> equities = Equity.loadFrom(inState);
        if (equities != null){
            mCurrentEquities = equities;
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Equity.saveTo(outState, mCurrentEquities);
        outState.putBoolean("SEARCH_FOCUS", mSearchView.hasFocus());
        outState.putString("SEARCH_TEXT", mSearchView.getQuery().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        finishWithResult(null);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stock_query, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        MenuItemCompat.expandActionView(searchItem);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setIconified(false);
        if (mSearchViewTextToSet != null) {
            mSearchView.setQuery(mSearchViewTextToSet, false);
            if (mRemoveSearchViewFocus)
                mSearchView.clearFocus();
            mSearchViewTextToSet = null;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishWithResult(null);
        }
        return true;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        hideKeyboard();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.isEmpty())
        {
            mCurrentEquities.clear();
            mAdapter.notifyDataSetChanged();
            return false;
        }

        mTask = new EquityQueryTask(mService, this);
        mTask.executeOnExecutor(THREAD_POOL_EXECUTOR, new EquityQuery(newText, mSupportedExchanges));
        return false;
    }

    @Override
    public void onStocksReceived(List<List<Equity>> stocks)
    {
        mCurrentEquities.clear();
        mCurrentEquities.addAll(stocks.get(0));
        mAdapter.notifyDataSetChanged();
    }

    private void showKeyboard()
    {
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).
                toggleSoftInput(InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void hideKeyboard()
    {
        View focusedView = getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    private void finishWithResult(Equity equity)
    {
        Intent returnIntent = new Intent();
        if (equity != null)
        {
            List<Equity> chosenEquities = new ArrayList<Equity>();
            chosenEquities.add(equity);
            Equity.saveTo(returnIntent, chosenEquities);
        }
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        finishWithResult((Equity)view.getTag());
    }

    private class EquityQueryRowAdapter extends EquityRowAdapter
    {
        EquityQueryRowAdapter(List<Equity> equities)
        {
            super(EquityQueryActivity.this, equities);
        }

        @Override
        public void onInitialStatus(Equity equity, ViewHolder holder)
        {
            if (mPortfolio.getWatchList().indexOf(equity) == -1)
                holder.changeStatus.setImageDrawable(ContextCompat.getDrawable(EquityQueryActivity.this, R.drawable.ic_add_24dp));
            else
                holder.changeStatus.setImageDrawable(ContextCompat.getDrawable(EquityQueryActivity.this, R.drawable.ic_remove_24dp));
        }

        @Override
        public void onChangeStatusPressed(Equity equity, ViewHolder holder)
        {
            WatchList watchList = mPortfolio.getWatchList();
            int curStockIndex = watchList.indexOf(equity);
            if (curStockIndex == -1)
            {
                if (watchList.add(equity))
                    holder.changeStatus.setImageDrawable(ContextCompat.getDrawable(EquityQueryActivity.this, R.drawable.ic_remove_24dp));
            }
            else
            {
                if (watchList.remove(curStockIndex) != null)
                    holder.changeStatus.setImageDrawable(ContextCompat.getDrawable(EquityQueryActivity.this, R.drawable.ic_add_24dp));
            }
        }
    }
}

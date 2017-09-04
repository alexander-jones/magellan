package com.magellan.magellan.stock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.magellan.magellan.ApplicationContext;
import com.magellan.magellan.R;
import com.magellan.magellan.service.yahoo.YahooService;

import java.util.ArrayList;
import java.util.List;

public class StockQueryActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, IStockQueryListener, AdapterView.OnItemClickListener{

    private ListView mList;
    private StockQueryTask mTask;
    private StockAdapter mAdapter;
    private List<Stock> mCurrentStocks = new ArrayList<Stock>();
    private IStockService mService = new YahooService();
    private List<String> mSupportedExchanges = new ArrayList<String>();
    private Toolbar mToolbar;
    private SearchView mSearchView;

    private String mSearchViewTextToSet = null;
    private boolean mRemoveSearchViewFocus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_query);
        ApplicationContext.init(this);

        setTitle("");
        mList =(ListView) findViewById(R.id.search_list_view);
        mAdapter = new StockAdapter(this, mCurrentStocks);
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

        List<Stock> stocks = Stock.loadFrom(inState);
        if (stocks != null){
            mCurrentStocks = stocks;
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Stock.saveTo(outState, mCurrentStocks);
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
            return false;

        mTask = new StockQueryTask(mService, this);
        mTask.execute(new StockQuery(newText, mSupportedExchanges));
        return false;
    }

    @Override
    public void onStocksReceived(List<List<Stock>> stocks)
    {
        mCurrentStocks.clear();
        mCurrentStocks.addAll(stocks.get(0));
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

    private void finishWithResult(Stock stock)
    {
        Intent returnIntent = new Intent();
        if (stock != null)
        {
            List<Stock> chosenStocks = new ArrayList<Stock>();
            chosenStocks.add(stock);
            Stock.saveTo(returnIntent, chosenStocks);
        }
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        finishWithResult((Stock)((StockAdapter.ViewHolder)view.getTag()).addToWatchList.getTag());
    }
}

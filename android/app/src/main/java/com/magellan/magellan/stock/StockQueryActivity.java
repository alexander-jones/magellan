package com.magellan.magellan.stock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.magellan.magellan.R;
import com.magellan.magellan.service.yahoo.YahooService;

import java.util.ArrayList;
import java.util.List;

public class StockQueryActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, IStockQueryListener, AdapterView.OnItemClickListener{

    private ListView mList;
    private StockQueryTask mTask;
    private StockAdapter mAdapter;
    private List<IStock> mCurrentStocks = new ArrayList<IStock>();
    private IStockService mService = new YahooService();
    private List<String> mSupportedExchanges = new ArrayList<String>();
    private Toolbar mToolbar;
    SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_query);

        setTitle("");
        mList =(ListView) findViewById(R.id.search_list_view);
        mAdapter = new StockAdapter(this, mCurrentStocks);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSupportedExchanges.add("NYSE");
        mSupportedExchanges.add("NASDAQ");
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
    public void onStocksReceived(List<List<IStock>> stocks)
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

    private void finishWithResult(CharSequence stock)
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("stock", stock);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        finishWithResult(((StockAdapter.ViewHolder)view.getTag()).symbol.getText());
    }
}

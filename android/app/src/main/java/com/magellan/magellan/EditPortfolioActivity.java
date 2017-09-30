package com.magellan.magellan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.magellan.magellan.equity.Equity;

import java.util.ArrayList;
import java.util.List;

public class EditPortfolioActivity extends  AppCompatActivity {
    private PortfolioList mPortfolioList;
    private PortfolioList.Item mPortfolioItem;
    private int mPortfolioIndex;

    private EditText mEditPortfolioName;
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_portfolio);

        Intent intent = getIntent();
        mPortfolioIndex = intent.getIntExtra("PORTFOLIO", -1);

        mPortfolioList = ApplicationContext.getPortfolios(this);
        mPortfolioList.load();
        mPortfolioItem = mPortfolioList.get(mPortfolioIndex);
        mPortfolioItem.load();

        setTitle("");
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mEditPortfolioName = (EditText)findViewById(R.id.edit_portfolio_name);
        mEditPortfolioName.setText(mPortfolioItem.name);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        finishWithResult();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishWithResult();
        }
        return true;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    private void finishWithResult()
    {
        Intent returnIntent = new Intent();
        String newName = mEditPortfolioName.getText().toString();
        if (!newName.equals(mPortfolioItem.name))
            returnIntent.putExtra("NEW_NAME", newName);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
}

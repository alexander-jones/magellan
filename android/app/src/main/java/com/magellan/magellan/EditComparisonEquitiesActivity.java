package com.magellan.magellan;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.magellan.magellan.equity.Equity;

import java.util.ArrayList;
import java.util.List;

public class EditComparisonEquitiesActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_comparison_layers);
        ApplicationContext.init(this);

        setTitle("");
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        PagerAdapter adapter = new PagerAdapter();
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.pager_tabs);
        tabLayout.setupWithViewPager(viewPager);
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
    public void onBackPressed() {
        finishWithResult();
        super.onBackPressed();
    }

    private void finishWithResult()
    {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    public static class MajorIndicesFragment extends Fragment {

        private List<Equity> mEquities;
        private EquityEditLayerRowAdapter mAdapter;

        public MajorIndicesFragment() {
            mEquities = ApplicationContext.getMajorIndices();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Activity act = getActivity();
            ListView rootView = new ListView(act);
            mAdapter = new EquityEditLayerRowAdapter(act, mEquities);
            rootView.setAdapter(mAdapter);
            return rootView;
        }
    }

    public static class SectorIndicesFragment extends Fragment {

        private List<Equity> mEquities;
        private EquityEditLayerRowAdapter mAdapter;

        public SectorIndicesFragment() {
            mEquities = ApplicationContext.getSectorIndices();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Activity act = getActivity();
            ListView rootView = new ListView(act);
            mAdapter = new EquityEditLayerRowAdapter(act, mEquities);
            rootView.setAdapter(mAdapter);
            return rootView;
        }
    }

    public static class WatchListFragment extends Fragment {

        private List<Equity> mEquities;
        private EquityEditLayerRowAdapter mAdapter;

        public WatchListFragment() {
            mEquities = ApplicationContext.getWatchList();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Activity act = getActivity();
            ListView rootView = new ListView(act);
            mAdapter = new EquityEditLayerRowAdapter(act, mEquities);
            rootView.setAdapter(mAdapter);
            return rootView;
        }
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter() {
            super(EditComparisonEquitiesActivity.this.getSupportFragmentManager());
        }

        // This determines the fragment for each tab
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new MajorIndicesFragment();
            } else if (position == 1) {
                return new SectorIndicesFragment();
            } else
                return new WatchListFragment();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Major Indices";
                case 1:
                    return "Sector Indices";
                case 2:
                    return "Watchlist";
                default:
                    return null;
            }
        }
    }

    private static class EquityEditLayerRowAdapter extends EquityRowAdapter
    {
        private Context mContext;
        EquityEditLayerRowAdapter(Context context, List<Equity> equities)
        {
            super(context, equities);
            mContext = context;
        }

        private void changeColor(ViewHolder holder, int color)
        {
            holder.changeStatus.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            holder.symbol.setTextColor(color);
            holder.name.setTextColor(color);
        }

        @Override
        public void onInitialStatus(Equity equity, ViewHolder holder)
        {
            int curIndex = ApplicationContext.getComparisonEquityIndex(equity);
            if (curIndex == -1) {
                holder.changeStatus.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add_24dp));
                changeColor(holder, ContextCompat.getColor(mContext, R.color.colorPrimary));
            }
            else{
                holder.changeStatus.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_remove_24dp));
                changeColor(holder, ApplicationContext.getComparisonEquityColors().get(curIndex));
            }
        }

        @Override
        public void onChangeStatusPressed(Equity equity, ViewHolder holder)
        {
            int curIndex = ApplicationContext.getComparisonEquityIndex(equity);
            if (curIndex == -1)
            {
                final Equity givenEquity = equity;
                final ViewHolder givenHolder = holder;
                ColorPickerDialogBuilder
                    .with(mContext, R.style.AppTheme_AlertDialogTheme)
                    .setTitle("Choose color")
                    .initialColor(Color.WHITE)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setPositiveButton("OK", new ColorPickerClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                            if (ApplicationContext.addToComparisonEquities(givenEquity, selectedColor))
                            {
                                givenHolder.changeStatus.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_remove_24dp));
                                changeColor(givenHolder, selectedColor);
                            }
                        }
                    })
                    .setNegativeButton("CANCEL", null)
                    .build()
                    .show();

            }
            else
            {
                if (ApplicationContext.removeFromComparisonEquities(curIndex)) {
                    holder.changeStatus.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add_24dp));
                    changeColor(holder, ContextCompat.getColor(mContext, R.color.colorPrimary));
                }
            }
        }
    }
}
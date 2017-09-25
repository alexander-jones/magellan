package com.magellan.magellan;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

import java.util.List;

public class EditComparisonEquitiesActivity extends AppCompatActivity {

    private int mPortfolioIndex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_comparison_layers);

        Intent intent = getIntent();
        mPortfolioIndex = intent.getIntExtra("PORTFOLIO", -1);

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

        private ComparisonList mComparisonList;
        private List<Equity> mEquities;
        private EquityEditLayerRowAdapter mAdapter;

        public MajorIndicesFragment() {
            mEquities = ApplicationContext.getMajorIndices();
        }

        @Override
        public void onAttach(Context context)
        {
            super.onAttach(context);
            PortfolioList portfolioList = ApplicationContext.getPortfolios(context);
            portfolioList.load();
            int index = getArguments().getInt("PORTFOLIO_INDEX", -1);
            PortfolioList.Item item = portfolioList.get(index);
            item.load();
            mComparisonList = item.getComparisonList();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Activity act = getActivity();
            ListView rootView = new ListView(act);
            mAdapter = new EquityEditLayerRowAdapter(act, mComparisonList, mEquities);
            rootView.setAdapter(mAdapter);
            return rootView;
        }
    }

    public static class SectorIndicesFragment extends Fragment {

        private ComparisonList mComparisonList;
        private List<Equity> mEquities;
        private EquityEditLayerRowAdapter mAdapter;

        public SectorIndicesFragment() {
            mEquities = ApplicationContext.getSectorIndices();
        }

        @Override
        public void onAttach(Context context)
        {
            super.onAttach(context);
            PortfolioList portfolioList = ApplicationContext.getPortfolios(context);
            portfolioList.load();
            int index = getArguments().getInt("PORTFOLIO_INDEX", -1);
            PortfolioList.Item item = portfolioList.get(index);
            item.load();
            mComparisonList = item.getComparisonList();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Activity act = getActivity();
            ListView rootView = new ListView(act);
            mAdapter = new EquityEditLayerRowAdapter(act, mComparisonList, mEquities);
            rootView.setAdapter(mAdapter);
            return rootView;
        }
    }

    public static class WatchListFragment extends Fragment {

        PortfolioList.Item mPortfolioItem;
        private EquityEditLayerRowAdapter mAdapter;

        @Override
        public void onAttach(Context context)
        {
            super.onAttach(context);
            PortfolioList portfolioList = ApplicationContext.getPortfolios(context);
            portfolioList.load();
            int index = getArguments().getInt("PORTFOLIO_INDEX", -1);
            mPortfolioItem = portfolioList.get(index);
            mPortfolioItem.load();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Activity act = getActivity();
            ListView rootView = new ListView(act);
            mAdapter = new EquityEditLayerRowAdapter(act, mPortfolioItem.getComparisonList(), mPortfolioItem.getWatchList());
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
            Fragment frag;
            if (position == 0) {
                frag =  new MajorIndicesFragment();
            } else if (position == 1) {
                frag =  new SectorIndicesFragment();
            } else {
                frag = new WatchListFragment();
            }

            Bundle bundle = new Bundle();
            bundle.putInt("PORTFOLIO_INDEX", mPortfolioIndex);
            frag.setArguments(bundle);
            return frag;
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
        private ComparisonList mComparisonList;
        EquityEditLayerRowAdapter(Context context, ComparisonList list, List<Equity> equities)
        {
            super(context, equities);
            mContext = context;
            mComparisonList = list;
            mComparisonList.load();
        }

        private void changeColor(ViewHolder holder, int color)
        {
            holder.symbol.setTextColor(color);

            // draw name at 75% brightness
            float hsv[] =  new float[3];
            Color.colorToHSV(color, hsv);
            hsv[2] *= 0.75;
            holder.name.setTextColor(Color.HSVToColor(hsv));
        }

        @Override
        public void onInitialStatus(Equity equity, ViewHolder holder)
        {
            int currentIndex = -1;
            ComparisonList.Item item = null;
            for (int i = 0; i < mComparisonList.size(); ++i)
            {
                item = mComparisonList.get(i);;
                if (item.equity.equals(equity)) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex == -1) {
                holder.changeStatus.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add_24dp));
                changeColor(holder, ContextCompat.getColor(mContext, R.color.colorAccentPrimaryLight));
            }
            else{
                holder.changeStatus.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_remove_24dp));
                changeColor(holder, item.color);
            }
        }

        @Override
        public void onChangeStatusPressed(Equity equity, ViewHolder holder)
        {
            int currentIndex = -1;
            for (int i = 0; i < mComparisonList.size(); ++i)
            {
                ComparisonList.Item item = mComparisonList.get(i);;
                if (item.equity.equals(equity)) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex == -1)
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
                        if (mComparisonList.add(new ComparisonList.Item(givenEquity, selectedColor, true)))
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
                if (mComparisonList.remove(currentIndex) != null) {
                    holder.changeStatus.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add_24dp));
                    changeColor(holder, ContextCompat.getColor(mContext, R.color.colorAccentPrimaryLight));
                }
            }
        }
    }
}
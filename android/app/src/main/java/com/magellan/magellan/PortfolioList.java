package com.magellan.magellan;

import android.content.SharedPreferences;
import android.graphics.Color;

import com.magellan.magellan.equity.Equity;

import java.util.ArrayList;
import java.util.List;

import static com.magellan.magellan.ApplicationContext.getMajorIndices;

public class PortfolioList extends SharedPreferencesList<PortfolioList.Item>
{
    private static String STORAGE_PREFIX = "PORTFOLIO_LIST";

    public static class Item
    {
        public Item(String n)
        {
            name = n;
            mWatchList = new WatchList();
            mComparisonList = new ComparisonList();
        }

        public WatchList getWatchList()
        {
            return mWatchList;
        }

        public ComparisonList getComparisonList()
        {
            return mComparisonList;
        }

        public void load()
        {
            mWatchList.load();
            mComparisonList.load();
        }

        private void saveTo(SharedPreferences sharedPreferences, SharedPreferences.Editor editor, String prefix, int index)
        {
            editor.putString(prefix + "_NAME_" + Integer.toString(index), name);
            mWatchList.attach(sharedPreferences, prefix, index);
            mComparisonList.attach(sharedPreferences, prefix, index);
            mWatchList.save(editor);
            mComparisonList.save(editor);
        }

        private static Item loadFrom(SharedPreferences sp, String prefix, int index)
        {
            String name = sp.getString(prefix + "_NAME_" + Integer.toString(index), "");
            Item item = new Item(name);
            item.mWatchList.attach(sp, prefix, index);
            item.mComparisonList.attach(sp, prefix, index);
            return item;
        }

        String name;
        private WatchList mWatchList;
        private ComparisonList mComparisonList;
    }

    public PortfolioList(SharedPreferences sp)
    {
        super(sp, STORAGE_PREFIX, 0);
    }

    protected PortfolioList()
    {
        super();
    }

    public Item addDefaultItem(String key)
    {
        List<Equity> majEquities = ApplicationContext.getMajorIndices();
        int [] colors = {Color.parseColor("#4a81ff"), Color.parseColor("#ff8d28"), Color.parseColor("#d428ff")};

        Item item = new Item(key);
        ComparisonList comparisonList = item.getComparisonList();
        for (int i = 0 ; i < 3; i++)
            comparisonList.add(new ComparisonList.Item(majEquities.get(i), colors[i], true));
        add(item);
        return item;
    }

    @Override
    protected void attach(SharedPreferences prefs, String classPrefix, int i)
    {
        super.attach(prefs, classPrefix + STORAGE_PREFIX, i);
    }

    @Override
    protected void saveItem(SharedPreferences.Editor editor, int offset)
    {
        Item item = get(offset);
        item.saveTo(mSharedPreferences, editor, mPrefix, offset);
    }

    @Override
    protected PortfolioList.Item loadItem(int offset)
    {
        return Item.loadFrom(mSharedPreferences, mPrefix, offset);
    }
}

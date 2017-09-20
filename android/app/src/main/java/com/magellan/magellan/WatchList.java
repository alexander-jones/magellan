package com.magellan.magellan;

import android.content.Context;
import android.content.SharedPreferences;

import com.magellan.magellan.equity.Equity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class WatchList
{
    private static String WATCHLIST_PREFIX = "WATCHLIST";
    public static HashMap<Integer,WatchList> mWatchLists = new HashMap<Integer,WatchList>();
    private static Context mContext;

    private int mWatchListGeneration = 0;
    private List<Equity> mWatchList = null;
    private SharedPreferences mSharedPreferences;

    public static void init(Context context)
    {
        mContext = context;
    }

    public static WatchList getOrCreate(int index)
    {
        WatchList ret = get(index);
        if (ret == null)
        {
            ret = new WatchList(mContext);
            mWatchLists.put(index, ret);
        }

        return ret;
    }

    public static WatchList get(int index)
    {
        if (mWatchLists == null)
            return null;

        return mWatchLists.get(index);
    }

    private WatchList(Context context)
    {
        mSharedPreferences = context.getSharedPreferences(WATCHLIST_PREFIX, Context.MODE_PRIVATE);
        mWatchList = new ArrayList<Equity>();
    }

    public int getGeneration()
    {

        return mWatchListGeneration;
    }

    public int getIndexOf(Equity equity)
    {
        int index = -1;
        for (int i =0; i < mWatchList.size(); ++i){
            if (equity.equals(mWatchList.get(i)))
            {
                index = i;
                break;
            }
        }
        return index;
    }

    public boolean move(int from, int to)
    {
        if (from < 0 || from >= mWatchList.size())
            return false;

        if (to < 0 || to >= mWatchList.size())
            return false;

        ++mWatchListGeneration;
        mWatchList.add(to, mWatchList.remove(from));
        save();
        return true;
    }

    public boolean remove(int stock)
    {
        if (stock < 0 || stock >= mWatchList.size())
            return false;

        ++mWatchListGeneration;
        mWatchList.remove(stock);
        save();
        return true;
    }

    public boolean add(Equity equity)
    {
        if (getIndexOf(equity) != -1)
            return false;

        ++mWatchListGeneration;
        mWatchList.add(equity);
        save();
        return true;
    }

    public List<Equity> getItems()
    {
        return Collections.unmodifiableList(mWatchList);
    }

    public void save()
    {
        SharedPreferences.Editor sp = mSharedPreferences.edit();
        sp.putInt(WATCHLIST_PREFIX + "_GENERATION", mWatchListGeneration);
        Equity.saveTo(sp, mWatchList, WATCHLIST_PREFIX);
        sp.commit();
    }

    public void load()
    {
        List<Equity> equities = Equity.loadFrom(mSharedPreferences, WATCHLIST_PREFIX);
        if (equities != null)
            mWatchList = equities;
        mWatchListGeneration = mSharedPreferences.getInt(WATCHLIST_PREFIX + "_GENERATION", 0);
    }
}

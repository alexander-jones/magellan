package com.magellan.magellan;

import android.content.Context;
import android.content.SharedPreferences;

import com.magellan.magellan.stock.Stock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplicationContext {

    private static String PREFERENCE_KEY = "APPLICATION_CONTEXT";

    private static Context mContext;
    private static int mWatchListGeneration = 0;
    private static List<Stock> mWatchList = null;
    private static int mSelectedStock = 0;
    private static SharedPreferences mSharedPreferences;

    public static void init(Context context)
    {
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        mWatchList = Stock.loadFrom(mSharedPreferences);
        mWatchListGeneration = mSharedPreferences.getInt("GENERATION", 0);
        if (mWatchList == null){
            mWatchList = new ArrayList<Stock>();
            mSelectedStock = -1;
        }
        else
            mSelectedStock = mSharedPreferences.getInt("SELECTED_STOCK", 0);
    }

    public static int getWatchListGeneration() { return mWatchListGeneration;}
    public static boolean setSelectedStock(int stock)
    {
        if (stock < 0 || stock >= mWatchList.size())
            return false;

        ++mWatchListGeneration;
        mSelectedStock = stock;
        SharedPreferences.Editor sp = mSharedPreferences.edit();
        sp.putInt("GENERATION", mWatchListGeneration);
        sp.putInt("SELECTED_STOCK", mSelectedStock);
        sp.commit();
        return true;
    }

    public static int getSelectedStock()
    {
        return mSelectedStock;
    }

    public static int getWatchListIndex(Stock stock)
    {
        int index = -1;
        for (int i =0; i < mWatchList.size(); ++i){
            if (stock.equals(mWatchList.get(i)))
            {
                index = i;
                break;
            }
        }
        return index;
    }

    public static boolean removeFromWatchList(int stock)
    {
        if (stock < 0 || stock >= mWatchList.size())
            return false;

        ++mWatchListGeneration;
        mWatchList.remove(stock);
        SharedPreferences.Editor sp = mSharedPreferences.edit();
        sp.putInt("GENERATION", mWatchListGeneration);
        Stock.saveTo(sp, mWatchList);
        if (mSelectedStock >= stock)
        {
            if (mWatchList.size() == 0)
                mSelectedStock = -1;
            else if (stock > 0)
                --mSelectedStock;
            else
                mSelectedStock = 0;
            sp.putInt("SELECTED_STOCK", mSelectedStock);
        }
        sp.commit();
        return true;
    }

    public static boolean removeFromWatchList(Stock stock)
    {
        int indexToRemove = getWatchListIndex(stock);
        if (indexToRemove == -1)
            return false;

        removeFromWatchList(indexToRemove);
        return true;
    }

    public static boolean addToWatchList(Stock stock)
    {
        for (Stock s : mWatchList){
            if (stock.equals(s))
                return false;
        }

        ++mWatchListGeneration;
        mWatchList.add(stock);
        SharedPreferences.Editor sp = mSharedPreferences.edit();
        sp.putInt("GENERATION", mWatchListGeneration);
        Stock.saveTo(sp, mWatchList);
        sp.commit();
        return true;
    }

    public static List<Stock> getWatchList()
    {
        return Collections.unmodifiableList(mWatchList);
    }

}

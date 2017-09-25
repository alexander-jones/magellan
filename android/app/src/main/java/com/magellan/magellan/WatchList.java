package com.magellan.magellan;

import android.content.Context;
import android.content.SharedPreferences;

import com.magellan.magellan.equity.Equity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WatchList extends SharedPreferencesList<Equity>
{
    private static String STORAGE_PREFIX = "WATCHLIST";

    public WatchList(SharedPreferences sp)
    {
        super(sp, STORAGE_PREFIX, 0);
    }

    protected WatchList()
    {
        super();
    }

    @Override
    protected void attach(SharedPreferences prefs, String classPrefix, int i)
    {
        super.attach(prefs, classPrefix + STORAGE_PREFIX, i);
    }

    @Override
    protected void saveItem(SharedPreferences.Editor editor, int offset)
    {
        get(offset).saveTo(editor, offset, mPrefix);
    }

    @Override
    protected Equity loadItem(int offset)
    {
        return Equity.loadFrom(mSharedPreferences, offset, mPrefix);
    }

}

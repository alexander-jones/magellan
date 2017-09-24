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

    private int mGeneration = 0;

    public WatchList(Context context, int storageSlot)
    {
        super(context, STORAGE_PREFIX + Integer.toString(storageSlot));
    }

    @Override
    protected void saveElement(SharedPreferences.Editor editor, int offset)
    {
        get(offset).saveTo(editor, offset);
    }

    @Override
    protected Equity loadElement(SharedPreferences sp, int offset)
    {
        return Equity.loadFrom(sp, offset);
    }

}

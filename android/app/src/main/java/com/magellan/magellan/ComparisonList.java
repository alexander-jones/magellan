package com.magellan.magellan;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.magellan.magellan.equity.Equity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ComparisonList extends SharedPreferencesList<ComparisonList.Item>
{
    private static String STORAGE_PREFIX = "COMPARISON_LIST";

    public static class Item
    {
        public Item(Equity e, int c, boolean enab)
        {
            equity = e;
            color = c;
            enabled = enab;
        }

        @Override
        public boolean equals(Object obj)
        {
            Item other = (Item)obj;
            return other.color == color && other.enabled == enabled && other.equity.equals(equity);
        }

        private void saveTo(SharedPreferences.Editor editor, String prefix, int offset)
        {
            equity.saveTo(editor, offset, prefix);
            editor.putInt(prefix + "_COLOR_" + Integer.toString(offset), color);
            editor.putBoolean(prefix + "_ENABLED_" + Integer.toString(offset), enabled);
        }

        private static Item loadFrom(SharedPreferences sp, String prefix, int offset)
        {
            Equity equity = Equity.loadFrom(sp, offset, prefix);
            int color = sp.getInt(prefix + "_COLOR_" + Integer.toString(offset), Color.WHITE);
            boolean enabled = sp.getBoolean(prefix + "_ENABLED_" + Integer.toString(offset), true);
            return new Item(equity, color, enabled);
        }

        boolean enabled;
        Equity equity;
        int color;
    }

    public ComparisonList(SharedPreferences sp)
    {
        super(sp, STORAGE_PREFIX, 0);
    }

    protected ComparisonList()
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
        ComparisonList.Item item = get(offset);
        item.saveTo(editor, mPrefix, offset);
    }

    @Override
    protected ComparisonList.Item loadItem(int offset)
    {
        return Item.loadFrom(mSharedPreferences, mPrefix, offset);
    }
}

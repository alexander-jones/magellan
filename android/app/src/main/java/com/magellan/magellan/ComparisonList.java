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

        public void saveTo(SharedPreferences.Editor editor, int offset)
        {
            equity.saveTo(editor, offset);
            editor.putInt("COLOR_" + Integer.toString(offset), color);
            editor.putBoolean("ENABLED_" + Integer.toString(offset), enabled);
        }

        public static Item loadFrom(SharedPreferences sp, int offset)
        {
            Equity equity = Equity.loadFrom(sp, offset);
            int color = sp.getInt("COLOR_" + Integer.toString(offset), Color.WHITE);
            boolean enabled = sp.getBoolean("ENABLED_" + Integer.toString(offset), true);
            return new Item(equity, color, enabled);
        }

        boolean enabled;
        Equity equity;
        int color;
    }

    public ComparisonList(Context context, int storageSlot)
    {
        super(context, STORAGE_PREFIX + Integer.toString(storageSlot));
    }

    @Override
    protected void saveElement(SharedPreferences.Editor editor, int offset)
    {
        ComparisonList.Item item = get(offset);
        item.saveTo(editor, offset);
    }

    @Override
    protected ComparisonList.Item loadElement(SharedPreferences sp, int offset)
    {
        return Item.loadFrom(sp, offset);
    }
}

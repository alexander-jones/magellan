package com.magellan.magellan.equity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class Equity {
    private static String COUNT_KEY = "EQUITY_COUNT";
    private static String SYMBOL_KEY = "EQUITY_SYMBOL";
    private static String NAME_KEY = "EQUITY_NAME";
    private static String EXCHANGE_KEY = "EQUITY_EXCHANGE";
    private static String TYPE_KEY = "EQUITY_TYPE";
    
    private String mSymbol;
    private String mName;
    private String mExchange;
    private String mType;

    public Equity(String symbol, String name, String exchange, String type)
    {
        mSymbol = symbol;
        mName = name;
        mExchange = exchange;
        mType = type;
    }

    @Override
    public boolean equals(Object object)
    {
        Equity equity = (Equity)object;
        if (equity == null)
            return false;

        if (!mSymbol.contentEquals(equity.mSymbol))
            return false;
        if (!mName.contentEquals(equity.mName))
            return false;
        if (!mExchange.contentEquals(equity.mExchange))
            return false;
        if (!mType.contentEquals(equity.mType))
            return false;

        return true;
    }

    public String getSymbol() {return mSymbol;}
    public String getName() {return mName;}
    public String getExchange() {return mExchange;}
    public String getType() {return mType;}

    public void saveTo(Bundle outState, int i) {saveTo(outState, i, "");}
    public void saveTo(Bundle outState, int i, String prefix)
    {
        outState.putString(prefix + SYMBOL_KEY + Integer.toString(i), getSymbol());
        outState.putString(prefix + NAME_KEY + Integer.toString(i), getName());
        outState.putString(prefix + EXCHANGE_KEY + Integer.toString(i), getExchange());
        outState.putString(prefix + TYPE_KEY + Integer.toString(i), getType());
    }

    public void saveTo(Intent outState, int i) {saveTo(outState, i, "");}
    public void saveTo(Intent outState, int i, String prefix)
    {
        outState.putExtra(prefix + SYMBOL_KEY + Integer.toString(i), getSymbol());
        outState.putExtra(prefix + NAME_KEY + Integer.toString(i), getName());
        outState.putExtra(prefix + EXCHANGE_KEY + Integer.toString(i), getExchange());
        outState.putExtra(prefix + TYPE_KEY + Integer.toString(i), getType());
    }


    public void saveTo(SharedPreferences.Editor outState, int i) {saveTo(outState, i, ""); }
    public void saveTo(SharedPreferences.Editor outState, int i, String prefix)
    {
        outState.putString(prefix + SYMBOL_KEY + Integer.toString(i), getSymbol());
        outState.putString(prefix + NAME_KEY + Integer.toString(i), getName());
        outState.putString(prefix + EXCHANGE_KEY + Integer.toString(i), getExchange());
        outState.putString(prefix + TYPE_KEY + Integer.toString(i), getType());
    }

    public static void saveTo(Intent outState, List<Equity> equities) {saveTo(outState, equities, ""); }
    public static void saveTo(Intent outState, List<Equity> equities, String prefix)
    {
        outState.putExtra(prefix + COUNT_KEY, equities.size());
        for (int i = 0; i < equities.size(); ++i)
            equities.get(i).saveTo(outState, i, prefix);
    }

    public static void saveTo(SharedPreferences.Editor outState, List<Equity> equities) {saveTo(outState, equities, ""); }
    public static void saveTo(SharedPreferences.Editor outState, List<Equity> equities, String prefix)
    {
        outState.putInt(prefix + COUNT_KEY, equities.size());
        for (int i = 0; i < equities.size(); ++i)
            equities.get(i).saveTo(outState, i, prefix);
    }

    public static void saveTo(Bundle outState, List<Equity> equities) {saveTo(outState, equities, ""); }
    public static void saveTo(Bundle outState, List<Equity> equities, String prefix)
    {
        outState.putInt(prefix + COUNT_KEY, equities.size());
        for (int i = 0; i < equities.size(); ++i)
            equities.get(i).saveTo(outState, i, prefix);
    }

    public static Equity loadFrom(Bundle inState, int i) {return loadFrom(inState, i, "");}
    public static Equity loadFrom(Bundle inState, int i, String prefix)
    {
        String symbol = inState.getString(prefix + SYMBOL_KEY + Integer.toString(i), null);
        if (symbol == null)
            return null;
        String company = inState.getString(prefix + NAME_KEY + Integer.toString(i), null);
        String exchange = inState.getString(prefix + EXCHANGE_KEY + Integer.toString(i), null);
        String type = inState.getString(prefix + TYPE_KEY + Integer.toString(i), null);
        return new Equity(symbol, company, exchange, type);
    }

    public static Equity loadFrom(Intent inState, int i) {return loadFrom(inState, i, "");}
    public static Equity loadFrom(Intent inState, int i, String prefix) { return loadFrom(inState.getExtras(), i, prefix); }

    public static Equity loadFrom(SharedPreferences inState, int i) {return loadFrom(inState, i, "");}
    public static Equity loadFrom(SharedPreferences inState, int i, String prefix)
    {
        String symbol = inState.getString(prefix + SYMBOL_KEY + Integer.toString(i), null);
        if (symbol == null)
            return null;
        String company = inState.getString(prefix + NAME_KEY + Integer.toString(i), null);
        String exchange = inState.getString(prefix + EXCHANGE_KEY + Integer.toString(i), null);
        String type = inState.getString(prefix + TYPE_KEY + Integer.toString(i), null);
        return new Equity(symbol, company, exchange, type);
    }

    public static List<Equity> loadFrom(Bundle inState)
    {
        return loadFrom(inState, "");
    }
    public static List<Equity> loadFrom(Bundle inState, String prefix)
    {
        int numResults = inState.getInt(prefix + COUNT_KEY , -1);
        if (numResults == -1)
            return null;

        List<Equity> ret = new ArrayList<Equity>(numResults);
        for (int i =0; i < numResults; ++i)
            ret.add(loadFrom(inState, i, prefix));
        return ret;
    }

    public static List<Equity> loadFrom(Intent inState)
    {
        return loadFrom(inState, "");
    }
    public static List<Equity> loadFrom(Intent inState, String prefix)
    {
        int numResults = inState.getIntExtra(prefix + COUNT_KEY , -1);
        if (numResults == -1)
            return null;

        List<Equity> ret = new ArrayList<Equity>(numResults);
        for (int i =0; i < numResults; ++i)
            ret.add(loadFrom(inState, i, prefix));
        return ret;
    }

    public static List<Equity> loadFrom(SharedPreferences inState)
    {
        return loadFrom(inState, "");
    }
    public static List<Equity> loadFrom(SharedPreferences inState, String prefix)
    {
        int numResults = inState.getInt(prefix + COUNT_KEY , -1);
        if (numResults == -1)
            return null;

        List<Equity> ret = new ArrayList<Equity>(numResults);
        for (int i =0; i < numResults; ++i)
            ret.add(loadFrom(inState, i, prefix));
        return ret;
    }

}

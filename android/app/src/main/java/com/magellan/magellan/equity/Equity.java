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

    public boolean equals(Equity equity)
    {
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

    public static List<Equity> loadFrom(Intent inState)
    {
        int numResults = inState.getIntExtra(COUNT_KEY , -1);
        if (numResults == -1)
            return null;

        List<Equity> ret = new ArrayList<Equity>(numResults);
        for (int i =0; i < numResults; ++i){

            String symbol = inState.getStringExtra(SYMBOL_KEY + Integer.toString(i));
            String company = inState.getStringExtra(NAME_KEY + Integer.toString(i));
            String exchange = inState.getStringExtra(EXCHANGE_KEY + Integer.toString(i));
            String type = inState.getStringExtra(TYPE_KEY + Integer.toString(i));
            ret.add(new Equity(symbol, company, exchange, type));
        }
        return ret;
    }

    public static void saveTo(Intent outState, List<Equity> equities)
    {
        outState.putExtra(COUNT_KEY, equities.size());
        for (int i = 0; i < equities.size(); ++i){
            outState.putExtra(SYMBOL_KEY + Integer.toString(i), equities.get(i).getSymbol());
            outState.putExtra(NAME_KEY + Integer.toString(i), equities.get(i).getName());
            outState.putExtra(EXCHANGE_KEY + Integer.toString(i), equities.get(i).getExchange());
            outState.putExtra(TYPE_KEY + Integer.toString(i), equities.get(i).getType());
        }
    }

    public static List<Equity> loadFrom(SharedPreferences inState)
    {
        int numResults = inState.getInt(COUNT_KEY , -1);
        if (numResults == -1)
            return null;

        List<Equity> ret = new ArrayList<Equity>(numResults);
        for (int i =0; i < numResults; ++i){

            String symbol = inState.getString(SYMBOL_KEY + Integer.toString(i), null);
            String company = inState.getString(NAME_KEY + Integer.toString(i), null);
            String exchange = inState.getString(EXCHANGE_KEY + Integer.toString(i), null);
            String type = inState.getString(TYPE_KEY + Integer.toString(i), null);
            ret.add(new Equity(symbol, company, exchange, type));
        }
        return ret;
    }

    public static void saveTo(SharedPreferences.Editor outState, List<Equity> equities)
    {
        outState.putInt(COUNT_KEY, equities.size());
        for (int i = 0; i < equities.size(); ++i){
            outState.putString(SYMBOL_KEY + Integer.toString(i), equities.get(i).getSymbol());
            outState.putString(NAME_KEY + Integer.toString(i), equities.get(i).getName());
            outState.putString(EXCHANGE_KEY + Integer.toString(i), equities.get(i).getExchange());
            outState.putString(TYPE_KEY + Integer.toString(i), equities.get(i).getType());
        }
    }

    public static List<Equity> loadFrom(Bundle inState)
    {
        int numResults = inState.getInt(COUNT_KEY , -1);
        if (numResults == -1)
            return null;

        List<Equity> ret = new ArrayList<Equity>(numResults);
        for (int i =0; i < numResults; ++i){

            String symbol = inState.getString(SYMBOL_KEY + Integer.toString(i), null);
            String company = inState.getString(NAME_KEY + Integer.toString(i), null);
            String exchange = inState.getString(EXCHANGE_KEY + Integer.toString(i), null);
            String type = inState.getString(TYPE_KEY + Integer.toString(i), null);
            ret.add(new Equity(symbol, company, exchange, type));
        }
        return ret;
    }

    public static void saveTo(Bundle outState, List<Equity> equities)
    {
        outState.putInt(COUNT_KEY, equities.size());
        for (int i = 0; i < equities.size(); ++i){
            outState.putString(SYMBOL_KEY + Integer.toString(i), equities.get(i).getSymbol());
            outState.putString(NAME_KEY + Integer.toString(i), equities.get(i).getName());
            outState.putString(EXCHANGE_KEY + Integer.toString(i), equities.get(i).getExchange());
            outState.putString(TYPE_KEY + Integer.toString(i), equities.get(i).getType());
        }
    }
}

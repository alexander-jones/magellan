package com.magellan.magellan.stock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class Stock {
    private static String COUNT_KEY = "STOCK_";
    private static String ITEM_PREFIX = "STOCK_";
    
    private String mSymbol;
    private String mCompany;
    private String mExchange;
    private String mType;

    public Stock(String symbol, String company, String exchange, String type)
    {
        mSymbol = symbol;
        mCompany = company;
        mExchange = exchange;
        mType = type;
    }

    public boolean equals(Stock stock)
    {
        if (!mSymbol.contentEquals(stock.mSymbol))
            return false;
        if (!mCompany.contentEquals(stock.mCompany))
            return false;
        if (!mExchange.contentEquals(stock.mExchange))
            return false;
        if (!mType.contentEquals(stock.mType))
            return false;

        return true;
    }

    public String getSymbol() {return mSymbol;}
    public String getCompany() {return mCompany;}
    public String getExchange() {return mExchange;}
    public String getType() {return mType;}

    public static List<Stock> loadFrom(Intent inState)
    {
        int numResults = inState.getIntExtra(COUNT_KEY , -1);
        if (numResults == -1)
            return null;

        List<Stock> ret = new ArrayList<Stock>(numResults);
        for (int i =0; i < numResults; ++i){

            String symbol = inState.getStringExtra(ITEM_PREFIX + Integer.toString(i) + "_SYMBOL");
            String company = inState.getStringExtra(ITEM_PREFIX + Integer.toString(i) + "_COMPANY");
            String exchange = inState.getStringExtra(ITEM_PREFIX + Integer.toString(i) + "_EXCHANGE");
            String type = inState.getStringExtra(ITEM_PREFIX + Integer.toString(i) + "_TYPE");
            ret.add(new Stock(symbol, company, exchange, type));
        }
        return ret;
    }

    public static void saveTo(Intent outState, List<Stock> stocks)
    {
        outState.putExtra(COUNT_KEY, stocks.size());
        for (int i =0; i < stocks.size(); ++i){
            outState.putExtra(ITEM_PREFIX + Integer.toString(i) + "_SYMBOL", stocks.get(i).getSymbol());
            outState.putExtra(ITEM_PREFIX + Integer.toString(i) + "_COMPANY", stocks.get(i).getCompany());
            outState.putExtra(ITEM_PREFIX + Integer.toString(i) + "_EXCHANGE", stocks.get(i).getExchange());
            outState.putExtra(ITEM_PREFIX + Integer.toString(i) + "_TYPE", stocks.get(i).getType());
        }
    }

    public static List<Stock> loadFrom(SharedPreferences inState)
    {
        int numResults = inState.getInt(COUNT_KEY , -1);
        if (numResults == -1)
            return null;

        List<Stock> ret = new ArrayList<Stock>(numResults);
        for (int i =0; i < numResults; ++i){

            String symbol = inState.getString(ITEM_PREFIX + Integer.toString(i) + "_SYMBOL", null);
            String company = inState.getString(ITEM_PREFIX + Integer.toString(i) + "_COMPANY", null);
            String exchange = inState.getString(ITEM_PREFIX + Integer.toString(i) + "_EXCHANGE", null);
            String type = inState.getString(ITEM_PREFIX + Integer.toString(i) + "_TYPE", null);
            ret.add(new Stock(symbol, company, exchange, type));
        }
        return ret;
    }

    public static void saveTo(SharedPreferences.Editor outState, List<Stock> stocks)
    {
        outState.putInt(COUNT_KEY, stocks.size());
        for (int i =0; i < stocks.size(); ++i){
            outState.putString(ITEM_PREFIX + Integer.toString(i) + "_SYMBOL", stocks.get(i).getSymbol());
            outState.putString(ITEM_PREFIX + Integer.toString(i) + "_COMPANY", stocks.get(i).getCompany());
            outState.putString(ITEM_PREFIX + Integer.toString(i) + "_EXCHANGE", stocks.get(i).getExchange());
            outState.putString(ITEM_PREFIX + Integer.toString(i) + "_TYPE", stocks.get(i).getType());
        }
    }

    public static List<Stock> loadFrom(Bundle inState)
    {
        int numResults = inState.getInt(COUNT_KEY , -1);
        if (numResults == -1)
            return null;

        List<Stock> ret = new ArrayList<Stock>(numResults);
        for (int i =0; i < numResults; ++i){

            String symbol = inState.getString(ITEM_PREFIX + Integer.toString(i) + "_SYMBOL", null);
            String company = inState.getString(ITEM_PREFIX + Integer.toString(i) + "_COMPANY", null);
            String exchange = inState.getString(ITEM_PREFIX + Integer.toString(i) + "_EXCHANGE", null);
            String type = inState.getString(ITEM_PREFIX + Integer.toString(i) + "_TYPE", null);
            ret.add(new Stock(symbol, company, exchange, type));
        }
        return ret;
    }

    public static void saveTo(Bundle outState, List<Stock> stocks)
    {
        outState.putInt(COUNT_KEY, stocks.size());
        for (int i =0; i < stocks.size(); ++i){
            outState.putString(ITEM_PREFIX + Integer.toString(i) + "_SYMBOL", stocks.get(i).getSymbol());
            outState.putString(ITEM_PREFIX + Integer.toString(i) + "_COMPANY", stocks.get(i).getCompany());
            outState.putString(ITEM_PREFIX + Integer.toString(i) + "_EXCHANGE", stocks.get(i).getExchange());
            outState.putString(ITEM_PREFIX + Integer.toString(i) + "_TYPE", stocks.get(i).getType());
        }
    }
}

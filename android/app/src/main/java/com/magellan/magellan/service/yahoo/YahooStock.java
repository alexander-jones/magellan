package com.magellan.magellan.service.yahoo;

import com.magellan.magellan.stock.IStock;

public class YahooStock implements IStock {
    private String mSymbol;
    private String mExchange;
    private String mType;
    private String mCompany;

    public YahooStock(String symbol, String company, String exchange, String type)
    {
        mSymbol = symbol;
        mCompany = company;
        mExchange = exchange;
        mType = type;
    }

    public String getCompany() { return mCompany;}

    public String getSymbol()
    {
        return mSymbol;
    }

    public String getExchange()
    {
        return mExchange;
    }

    public String getType()
    {
        return mType;
    }

}

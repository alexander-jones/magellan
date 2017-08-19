package com.magellan.magellan.stock;

public class Stock {
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
    public String getSymbol() {return mSymbol;}
    public String getCompany() {return mCompany;}
    public String getExchange() {return mExchange;}
    public String getType() {return mType;}
}

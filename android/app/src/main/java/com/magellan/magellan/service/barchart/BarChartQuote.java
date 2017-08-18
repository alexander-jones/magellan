package com.magellan.magellan.service.barchart;

import com.barchart.ondemand.api.responses.HistoryBar;
import com.magellan.magellan.quote.IQuote;

import org.joda.time.DateTime;

public class BarChartQuote implements IQuote
{
    private HistoryBar mSource;
    public BarChartQuote(HistoryBar source) { mSource = source;}
    public DateTime getTime() { return DateTime.parse(mSource.getTimestamp());}
    public float getClose() { return (float)mSource.getClose();}
    public float getOpen() { return (float)mSource.getOpen();}
    public float getLow() { return (float)mSource.getLow();}
    public float getHigh() { return (float)mSource.getHigh();}
    public int getVolume() { return mSource.getVolume();}
}
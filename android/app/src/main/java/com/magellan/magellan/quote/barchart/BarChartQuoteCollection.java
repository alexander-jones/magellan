package com.magellan.magellan.quote.barchart;

import com.barchart.ondemand.api.responses.HistoryBar;
import com.magellan.magellan.quote.IQuote;
import com.magellan.magellan.quote.IQuoteCollection;

import java.util.Collection;

public class BarChartQuoteCollection implements IQuoteCollection
{
    private BarChartQuote [] mSource;
    public BarChartQuoteCollection(Collection<HistoryBar> source)
    {
        // not sure if it is better to lazily construct the BarCharQuote objects
        // but I'd imagine anyone making the query wants to access ALL of the data
        // and may want to access individual data points multiple times
        // so we'll do the construction up front.
        HistoryBar [] tmp = new HistoryBar [source.size()];
        source.toArray(tmp);

        mSource = new BarChartQuote [source.size()];
        for (int i = 0; i < source.size(); i++)
            mSource[i] = new BarChartQuote(tmp[i]);
    }
    public IQuote get(int i) { return mSource[i];}
    public int size() {return mSource.length;}
}
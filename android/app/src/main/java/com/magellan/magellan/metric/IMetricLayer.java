package com.magellan.magellan.metric;

import com.github.mikephil.charting.data.CombinedData;
import com.magellan.magellan.quote.Quote;

import java.util.List;

public interface IMetricLayer {
    public void onDrawQuotes(List<Quote> quotes, int missingStartSteps, int missingEndSteps, CombinedData chartData);

    public String getShortName();
    public String getName();
}

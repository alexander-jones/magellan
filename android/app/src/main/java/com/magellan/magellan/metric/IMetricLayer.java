package com.magellan.magellan.metric;

import com.github.mikephil.charting.data.CombinedData;
import com.magellan.magellan.quote.IQuoteCollection;

public interface IMetricLayer {
    public void onDrawQuotes(IQuoteCollection quotes, int missingStartSteps, int missingEndSteps, CombinedData chartData);

    public String getShortName();
    public String getName();
}

package com.magellan.magellan;

import android.content.Context;
import com.github.mikephil.charting.data.CombinedData;

public class Metric {

    public static interface IChartLayer {
        public void init(Context context, CombinedData priceChartData, CombinedData volumeChartData);
        public void onQuoteResults(Stock.IQuoteCollection quotes, Stock.QuoteCollectionContext context);
    }

}

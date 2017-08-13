package com.magellan.magellan;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;

public class VolumeMetric {
    public static class ChartLayer implements Metric.IChartLayer {

        private Context mContext;
        private CombinedData mVolumeChartData;
        private BarData mBarData = null;

        public void init(Context context, CombinedData priceChartData, CombinedData volumeChartData)
        {
            mContext = context;
            mVolumeChartData = volumeChartData;
        }

        public void onQuoteResults(Stock.IQuoteCollection stockHistory, Stock.QuoteCollectionContext context) {

            Stock.IQuote initialQuote = stockHistory.get(0);
            Stock.IQuote finalQuote = stockHistory.get(stockHistory.size() -1);

            ArrayList<IBarDataSet> volumeDataSets = new ArrayList<IBarDataSet>();
            ArrayList<BarEntry> volumeValues = new ArrayList<BarEntry>();

            float startingOpen = initialQuote.getOpen();
            for (int j = 0; j < context.missingStartSteps; ++j)
                volumeValues.add(new BarEntry(j, 0, null));

            for (int j = context.missingStartSteps; j < stockHistory.size() + context.missingStartSteps; ++j)
            {
                Stock.IQuote quote = stockHistory.get(j - context.missingStartSteps);
                volumeValues.add(new BarEntry(j, (float)quote.getVolume(), quote));
            }

            float finalClose = finalQuote.getClose();
            for (int j = context.missingStartSteps +stockHistory.size() ; j < stockHistory.size() + context.missingStartSteps + context.missingEndSteps; ++j)
                volumeValues.add(new BarEntry(j, 0, null));

            BarDataSet barSet = new BarDataSet(volumeValues, "");
            barSet.setHighlightEnabled(true);
            barSet.setDrawIcons(false);
            barSet.setDrawValues(false);
            barSet.setHighLightColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            barSet.setColor(ContextCompat.getColor(mContext, R.color.colorSecondary));
            volumeDataSets.add(barSet);

            BarData bd = new BarData(volumeDataSets);
            bd.setBarWidth(1);
            mVolumeChartData.setData(bd);
        }
    }
}

package com.magellan.magellan;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;

import java.util.ArrayList;

public class VolumeMetric {

    /*public static String valueToString(int volume)
    {
        float absVolume = Math.abs(volume);
        if (absVolume >= 1000000000) // you never know amiright?
            return String.format("%.2fB", (float)volume / 1000000000.0f);
        else if (absVolume >= 1000000)
            return String.format("%.2fM", (float)volume / 1000000.0f);
        else if (absVolume >= 1000)
            return String.format("%.2fK", (float)volume / 1000.0f);
        else
            return String.format("%d", volume);
    }*/

    public static String valueToString(int volume)
    {
        float absVolume = Math.abs(volume);
        if (absVolume >= 1000000000) // you never know amiright?
            return String.format("%dfB", volume / 1000000000);
        else if (absVolume >= 1000000)
            return String.format("%dM",  volume / 1000000);
        else if (absVolume >= 1000)
            return String.format("%dK",  volume / 1000);
        else
            return String.format("%d",  volume);
    }

    public static String valueDiffToString(int volumeDiff)
    {
        String baseStr = valueToString(volumeDiff);
        if (volumeDiff >  0)
            return "+" + baseStr;
        return baseStr;
    }

    public static class ValueFormatter implements IAxisValueFormatter{

        @Override
        public String getFormattedValue(float value, AxisBase axis)
        {
            return valueToString((int)value);
        }
    }

    public static class BarChartLayer implements Metric.IChartLayer {

        private Context mContext;
        private CombinedData mVolumeChartData;

        public void init(Context context, CombinedData volumeChartData)
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
            barSet.setBarBorderColor(ContextCompat.getColor(mContext, R.color.colorAccentPrimary));
            barSet.setColor(ContextCompat.getColor(mContext, R.color.colorAccentPrimaryDark));
            barSet.setBarBorderWidth(0.1f);
            volumeDataSets.add(barSet);

            BarData data = mVolumeChartData.getBarData();
            if (data == null){
                ArrayList<IBarDataSet> priceDataSets = new ArrayList<IBarDataSet>();
                priceDataSets.add(barSet);
                data = new BarData(priceDataSets);
                data.setBarWidth(1);
            }
            else
                data.addDataSet(barSet);
            mVolumeChartData.setData(data);
        }
    }
}

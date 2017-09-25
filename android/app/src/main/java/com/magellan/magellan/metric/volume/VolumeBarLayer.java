package com.magellan.magellan.metric.volume;


import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.magellan.magellan.R;
import com.magellan.magellan.metric.IMetricLayer;
import com.magellan.magellan.quote.Quote;

import java.util.ArrayList;
import java.util.List;

public class VolumeBarLayer implements IMetricLayer {

    public String getName()
    {
        return "Bar Chart";
    }

    public String getShortName()
    {
        return "BAR";
    }

    private Context mContext;
    public VolumeBarLayer(Context context)
    {
        mContext = context;
    }

    public void onDrawQuotes(List<Quote> quotes, int missingStartSteps, int missingEndSteps, CombinedData chartData) {
        Quote initialQuote = quotes.get(0);
        Quote finalQuote = quotes.get(quotes.size() - 1);

        ArrayList<IBarDataSet> volumeDataSets = new ArrayList<IBarDataSet>();
        ArrayList<BarEntry> volumeValues = new ArrayList<BarEntry>();

        float startingOpen = initialQuote.open;
        for (int j = 0; j < missingStartSteps; ++j)
            volumeValues.add(new BarEntry(j, 0, null));

        for (int j = missingStartSteps; j < quotes.size() + missingStartSteps; ++j) {
            Quote quote = quotes.get(j - missingStartSteps);
            volumeValues.add(new BarEntry(j, (float) quote.volume, quote));
        }

        float finalClose = finalQuote.close;
        for (int j = missingStartSteps + quotes.size(); j < quotes.size() + missingStartSteps + missingEndSteps; ++j)
            volumeValues.add(new BarEntry(j, 0, null));

        BarDataSet barSet = new BarDataSet(volumeValues, "");
        barSet.setHighlightEnabled(true);
        barSet.setDrawIcons(false);
        barSet.setDrawValues(false);
        barSet.setHighLightColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        barSet.setBarBorderColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        barSet.setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        barSet.setBarBorderWidth(0.1f);
        volumeDataSets.add(barSet);

        BarData data = chartData.getBarData();
        if (data == null) {
            ArrayList<IBarDataSet> priceDataSets = new ArrayList<IBarDataSet>();
            priceDataSets.add(barSet);
            data = new BarData(priceDataSets);
            data.setBarWidth(1);
        } else
            data.addDataSet(barSet);
        chartData.setData(data);
    }
}
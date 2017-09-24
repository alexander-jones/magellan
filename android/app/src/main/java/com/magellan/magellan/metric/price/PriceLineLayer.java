package com.magellan.magellan.metric.price;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.magellan.magellan.R;
import com.magellan.magellan.metric.ILineDataSetStyler;
import com.magellan.magellan.metric.IMetricLayer;
import com.magellan.magellan.quote.Quote;

import java.util.ArrayList;
import java.util.List;

public class PriceLineLayer implements IMetricLayer
{
    private CombinedData mPriceChartData;
    private ILineDataSetStyler mStyler;

    public String getName()
    {
        return "Line Chart";
    }

    public String getShortName()
    {
        return "LIN";
    }

    public PriceLineLayer(ILineDataSetStyler styler)
    {
        mStyler = styler;
    }

    public void setStyler(ILineDataSetStyler styler)
    {
        mStyler = styler;
    }

    public void onDrawQuotes(List<Quote> quotes, int missingStartSteps, int missingEndSteps, CombinedData chartData) {
        Quote initialQuote = quotes.get(0);
        Quote finalQuote = quotes.get(quotes.size() - 1);
        ArrayList<Entry> priceValues = new ArrayList<Entry>();

        for (int j = missingStartSteps; j < quotes.size() + missingStartSteps; ++j) {
            Quote quote = quotes.get(j - missingStartSteps);
            priceValues.add(new Entry(j, (float) quote.close, quote));
        }

        LineDataSet missingDataSet = null;
        if (missingStartSteps > 0 || missingEndSteps > 0)
        {
            ArrayList<Entry> missingPriceValues = new ArrayList<Entry>();
            float startingOpen = initialQuote.open;
            for (int j = 0; j < missingStartSteps; ++j)
                missingPriceValues.add(new Entry(j, startingOpen, null));

            float finalClose = finalQuote.close;
            for (int j = missingStartSteps + quotes.size(); j < quotes.size() + missingStartSteps + missingEndSteps; ++j)
                missingPriceValues.add(new Entry(j, finalClose, null));

            missingDataSet = new LineDataSet(missingPriceValues, "");
            missingDataSet.setDrawIcons(false);
            missingDataSet.setHighlightEnabled(false);
            missingDataSet.setColor(Color.TRANSPARENT);
            missingDataSet.setDrawCircles(false);
            missingDataSet.setDrawValues(false);
        }

        LineDataSet lineSet = new LineDataSet(priceValues, "");
        mStyler.onApply(lineSet);

        LineData data = chartData.getLineData();
        if (data == null)
            data = new LineData();
        data.addDataSet(lineSet);
        if (missingDataSet != null)
            data.addDataSet(missingDataSet);
        chartData.setData(data);
    }
}
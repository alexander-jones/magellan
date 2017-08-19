package com.magellan.magellan.metric.price;

import android.content.Context;
import android.graphics.DashPathEffect;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.magellan.magellan.R;
import com.magellan.magellan.metric.IMetricLayer;
import com.magellan.magellan.quote.Quote;

import java.util.ArrayList;
import java.util.List;

public class PriceLineLayer implements IMetricLayer
{
    private Context mContext;
    private CombinedData mPriceChartData;

    public String getName()
    {
        return "Line Chart";
    }

    public String getShortName()
    {
        return "LIN";
    }

    public PriceLineLayer(Context context)
    {
        mContext = context;
    }

    public void onDrawQuotes(List<Quote> quotes, int missingStartSteps, int missingEndSteps, CombinedData chartData) {
        Quote initialQuote = quotes.get(0);
        Quote finalQuote = quotes.get(quotes.size() - 1);
        ArrayList<Entry> priceValues = new ArrayList<Entry>();

        float startingOpen = initialQuote.getOpen();
        for (int j = 0; j < missingStartSteps; ++j)
            priceValues.add(new Entry(j, startingOpen, null));

        for (int j = missingStartSteps; j < quotes.size() + missingStartSteps; ++j) {
            Quote quote = quotes.get(j - missingStartSteps);
            priceValues.add(new Entry(j, (float) quote.getClose(), quote));
        }

        float finalClose = finalQuote.getClose();
        for (int j = missingStartSteps + quotes.size(); j < quotes.size() + missingStartSteps + missingEndSteps; ++j)
            priceValues.add(new Entry(j, finalClose, null));

        LineDataSet lineSet = new LineDataSet(priceValues, "");
        lineSet.setDrawIcons(false);
        lineSet.setHighlightEnabled(true);
        lineSet.disableDashedLine();
        lineSet.enableDashedHighlightLine(10f, 5f, 0f);
        lineSet.setHighLightColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        lineSet.setColor(ContextCompat.getColor(mContext, R.color.colorAccentPrimary));
        lineSet.setFillColor(ContextCompat.getColor(mContext, R.color.colorAccentPrimary));
        lineSet.setLineWidth(1f);
        lineSet.setValueTextSize(9f);
        lineSet.setDrawFilled(true);
        lineSet.setFormLineWidth(1f);
        lineSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        lineSet.setFormSize(15.f);
        lineSet.setDrawCircles(false);
        lineSet.setDrawValues(false);

        LineData data = chartData.getLineData();
        if (data == null){
            ArrayList<ILineDataSet> priceDataSets = new ArrayList<ILineDataSet>();
            priceDataSets.add(lineSet);
            data = new LineData(priceDataSets);
        }
        else
            data.addDataSet(lineSet);
        chartData.setData(data);
    }
}
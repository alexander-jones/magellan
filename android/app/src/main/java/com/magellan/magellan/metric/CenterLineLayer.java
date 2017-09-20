package com.magellan.magellan.metric;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.magellan.magellan.R;
import com.magellan.magellan.quote.Quote;

import java.util.ArrayList;
import java.util.List;

public class CenterLineLayer implements IMetricLayer {
    private Float mCenter = null;
    private Context mContext;
    public CenterLineLayer(Context context)
    {
        mContext = context;
    }

    public void setCenter(Float center)
    {
        mCenter = center;
    }

    public void onDrawQuotes(List<Quote> quotes, int missingStartSteps, int missingEndSteps, CombinedData chartData)
    {
        ArrayList<Entry> centerLineValues = new ArrayList<Entry>();
        if (mCenter == null)
        {
            Quote initialQuote = quotes.get(0);
            float startingOpen = initialQuote.open;
            centerLineValues.add(new Entry(0, startingOpen, null));
            centerLineValues.add(new Entry((quotes.size() + missingStartSteps + missingEndSteps) - 1, startingOpen, null));
        }
        else
        {
            centerLineValues.add(new Entry(0, mCenter, null));
            centerLineValues.add(new Entry((quotes.size() + missingStartSteps + missingEndSteps) - 1, mCenter, null));
        }

        LineDataSet centerLineSet = new LineDataSet(centerLineValues, "");
        centerLineSet.setDrawIcons(false);
        centerLineSet.setHighlightEnabled(false);
        centerLineSet.enableDashedLine(10f, 10f, 0f);
        centerLineSet.setColor(ContextCompat.getColor(mContext, R.color.colorAccentPrimaryLight));
        centerLineSet.setFillColor(ContextCompat.getColor(mContext, R.color.colorAccentPrimaryLight));
        centerLineSet.setLineWidth(1f);
        centerLineSet.setDrawCircles(false);
        centerLineSet.setDrawValues(false);

        LineData data = chartData.getLineData();
        if (data == null){
            ArrayList<ILineDataSet> priceDataSets = new ArrayList<ILineDataSet>();
            priceDataSets.add(centerLineSet);
            data = new LineData(priceDataSets);
        }
        else {
            data.addDataSet(centerLineSet);
        }
        chartData.setData(data);
    }

    public String getShortName()
    {
        return "CENT";
    }

    public String getName()
    {
        return "Center";
    }
}

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

public class CenterLineLayer{
    private float mCenter = 0.0f;
    private Context mContext;
    public CenterLineLayer(Context context)
    {
        mContext = context;
    }

    public void setCenter(Float center)
    {
        mCenter = center;
    }

    public void draw(int steps, CombinedData chartData)
    {
        ArrayList<Entry> centerLineValues = new ArrayList<Entry>();
        centerLineValues.add(new Entry(0, mCenter, null));
        centerLineValues.add(new Entry(steps - 1, mCenter, null));

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
        if (data == null)
            data = new LineData();
        data.addDataSet(centerLineSet);
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

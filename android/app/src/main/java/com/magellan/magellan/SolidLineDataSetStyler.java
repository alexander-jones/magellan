package com.magellan.magellan;

import com.github.mikephil.charting.data.LineDataSet;
import com.magellan.magellan.metric.ILineDataSetStyler;

public class SolidLineDataSetStyler implements ILineDataSetStyler {

    private int mLineColor;
    public SolidLineDataSetStyler(int color)
    {
        mLineColor = color;
    }

    @Override
    public void onApply(LineDataSet lineSet) {
        lineSet.setDrawCircles(false);
        lineSet.setDrawValues(false);
        lineSet.setDrawFilled(false);
        lineSet.setDrawIcons(false);
        lineSet.setHighlightEnabled(false);
        lineSet.disableDashedLine();
        lineSet.setColor(mLineColor);
        lineSet.setLineWidth(1f);
    }
}
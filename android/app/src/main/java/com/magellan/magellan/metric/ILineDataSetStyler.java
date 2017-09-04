package com.magellan.magellan.metric;

import android.content.Context;

import com.github.mikephil.charting.data.LineDataSet;

public interface ILineDataSetStyler {
    void onApply(LineDataSet lineSet);
}

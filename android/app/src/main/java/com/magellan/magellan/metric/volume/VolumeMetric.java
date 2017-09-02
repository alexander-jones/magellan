package com.magellan.magellan.metric.volume;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class VolumeMetric {

    public static String valueToString(long volume)
    {
        if (volume >= 1000000000) // you never know amiright?
            return String.format("%.2fB", (double)volume / 1000000000.0f);
        else if (volume >= 1000000)
            return String.format("%.2fM", (float)volume / 1000000.0f);
        else if (volume >= 1000)
            return String.format("%.2fK", (float)volume / 1000.0f);
        else
            return String.format("%d", volume);
    }

    public static String valueDiffToString(int volumeDiff)
    {
        String baseStr = valueToString(volumeDiff);
        if (volumeDiff >  0)
            return "+" + baseStr;
        return baseStr;
    }

    public static class AxisValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis)
        {
            return valueToString((int)value);
        }
    }
}
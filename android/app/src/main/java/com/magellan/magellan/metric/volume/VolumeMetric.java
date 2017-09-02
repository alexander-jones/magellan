package com.magellan.magellan.metric.volume;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class VolumeMetric {

    public static String valueToString(long volume, int decimalDigits)
    {
        String format = decimalDigits == 0 ? "%d" : "%." + decimalDigits + "f";

        double val;
        if (volume >= 1000000000) // you never know amiright?
        {
            val = (double)volume / 1000000000.0;
            format += "B";
        }
        else if (volume >= 1000000)
        {
            val = (double)volume / 1000000.0;
            format += "M";
        }
        else if (volume >= 1000)
        {
            val = (double)volume / 1000.0;
            format += "K";
        }
        else
            val = (double)volume;

        if  (decimalDigits == 0)
            return String.format(format, (int)val);
        else
            return String.format(format, val);
    }

    public static String valueToString(long volume)
    {
        return valueToString(volume, 2);
    }


    public static String valueDiffToString(long volumeDiff)
    {
        String baseStr = valueToString(volumeDiff);
        if (volumeDiff >  0)
            return "+" + baseStr;
        return baseStr;
    }

    public static class AxisValueFormatter implements IAxisValueFormatter {
        private int mDigits;
        public AxisValueFormatter(int decimalDigits) {mDigits = decimalDigits;}
        public AxisValueFormatter() {mDigits = 2;}
        @Override
        public String getFormattedValue(float value, AxisBase axis)
        {
            return valueToString((int)value, mDigits);
        }
    }
}
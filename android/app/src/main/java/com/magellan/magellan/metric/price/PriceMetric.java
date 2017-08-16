package com.magellan.magellan.metric.price;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class PriceMetric {

    public static String valueToString(float price)
    {
        return String.format("$%.2f", price);
    }

    public static String valueDiffToString(float priceDiff)
    {
        if (priceDiff < 0.0)
            return "-" + valueToString(Math.abs(priceDiff));
        return "+" + valueToString(priceDiff);
    }

    public static class AxisValueFormatter implements IAxisValueFormatter {
        @Override
        public String getFormattedValue(float value, AxisBase axis)
        {
            return valueToString(value);
        }
    }
}

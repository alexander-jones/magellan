package com.magellan.magellan.metric.price;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class PriceMetric {

    public static String valueToString(float price, int decimalDigits)
    {
        if (price >= 1000) // you never know amiright?
            return String.format("$%." + Integer.toString(decimalDigits) + "fK", price / 1000.0f);
        return String.format("$%." + Integer.toString(decimalDigits) + "f", price);
    }

    public static String valueToString(float price)
    {
        return valueToString(price, 2);
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

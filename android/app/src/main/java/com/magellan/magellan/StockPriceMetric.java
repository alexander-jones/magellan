package com.magellan.magellan;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class StockPriceMetric {

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

    public static class ValueFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis)
        {
            return valueToString(value);
        }
    }

    public static class LineChartLayer implements Metric.IChartLayer
    {
        private Context mContext;
        private CombinedData mPriceChartData;

        public void init(Context context, CombinedData priceChartData)
        {
            mContext = context;
            mPriceChartData = priceChartData;
        }

        public void onQuoteResults(Stock.IQuoteCollection stockHistory, Stock.QuoteCollectionContext context) {
            Stock.IQuote initialQuote = stockHistory.get(0);
            Stock.IQuote finalQuote = stockHistory.get(stockHistory.size() - 1);
            ArrayList<Entry> priceValues = new ArrayList<Entry>();

            float startingOpen = initialQuote.getOpen();
            for (int j = 0; j < context.missingStartSteps; ++j)
                priceValues.add(new Entry(j, startingOpen, null));

            for (int j = context.missingStartSteps; j < stockHistory.size() + context.missingStartSteps; ++j) {
                Stock.IQuote quote = stockHistory.get(j - context.missingStartSteps);
                priceValues.add(new Entry(j, (float) quote.getClose(), quote));
            }

            float finalClose = finalQuote.getClose();
            for (int j = context.missingStartSteps + stockHistory.size(); j < stockHistory.size() + context.missingStartSteps + context.missingEndSteps; ++j)
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

            LineData data = mPriceChartData.getLineData();
            if (data == null){
                ArrayList<ILineDataSet> priceDataSets = new ArrayList<ILineDataSet>();
                priceDataSets.add(lineSet);
                data = new LineData(priceDataSets);
            }
            else
                data.addDataSet(lineSet);
            mPriceChartData.setData(data);
        }
    }

    public static class CandleChartLayer implements Metric.IChartLayer
    {
        private Context mContext;
        private CombinedData mPriceChartData;

        public void init(Context context, CombinedData priceChartData)
        {
            mContext = context;
            mPriceChartData = priceChartData;
        }

        public void onQuoteResults(Stock.IQuoteCollection stockHistory, Stock.QuoteCollectionContext context)
        {
            Stock.IQuote initialQuote = stockHistory.get(0);
            Stock.IQuote finalQuote = stockHistory.get(stockHistory.size() -1);

            ArrayList<CandleEntry> priceValues = new ArrayList<CandleEntry>();

            for (int j = 0; j < context.missingStartSteps; ++j)
                priceValues.add(new CandleEntry(j, initialQuote.getHigh(), initialQuote.getLow(), initialQuote.getOpen(), initialQuote.getClose(), null));

            for (int j = context.missingStartSteps; j < stockHistory.size() + context.missingStartSteps; ++j) {
                Stock.IQuote quote = stockHistory.get(j - context.missingStartSteps);
                priceValues.add(new CandleEntry(j, quote.getHigh(), quote.getLow(), quote.getOpen(), quote.getClose(), quote));
            }

            for (int j = context.missingStartSteps + stockHistory.size(); j < stockHistory.size() + context.missingStartSteps + context.missingEndSteps; ++j)
                priceValues.add(new CandleEntry(j, finalQuote.getHigh(), finalQuote.getLow(), finalQuote.getOpen(), finalQuote.getClose(), null));

            CandleDataSet candleSet = new CandleDataSet(priceValues, "");
            candleSet.setColor(Color.rgb(80, 80, 80));
            candleSet.setShadowWidth(0.7f);
            candleSet.setDecreasingPaintStyle(Paint.Style.FILL);
            candleSet.setDecreasingColor(ContextCompat.getColor(mContext, R.color.colorPriceDown));
            candleSet.setIncreasingColor(ContextCompat.getColor(mContext, R.color.colorPriceUp));
            candleSet.setHighLightColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            candleSet.setColor(ContextCompat.getColor(mContext, R.color.colorAccentPrimary));
            candleSet.setShadowColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
            candleSet.setNeutralColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            candleSet.setIncreasingPaintStyle(Paint.Style.STROKE);
            candleSet.setDrawIcons(false);
            candleSet.setHighlightEnabled(true);
            candleSet.enableDashedHighlightLine(10f, 5f, 0f);
            candleSet.setValueTextSize(9f);
            candleSet.setFormLineWidth(1f);
            candleSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            candleSet.setFormSize(15.f);
            candleSet.setDrawValues(false);

            CandleData data = mPriceChartData.getCandleData();
            if (data == null){
                ArrayList<ICandleDataSet> priceDataSets = new ArrayList<ICandleDataSet>();
                priceDataSets.add(candleSet);
                data = new CandleData(priceDataSets);
            }
            else
                data.addDataSet(candleSet);
            mPriceChartData.setData(data);
        }
    }
}

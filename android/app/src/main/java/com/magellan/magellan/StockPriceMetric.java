package com.magellan.magellan;

import android.content.Context;
import android.graphics.DashPathEffect;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class StockPriceMetric {

    public static enum ChartType
    {
        Candle,
        Line
    }

    public static class ChartLayer implements Metric.IChartLayer
    {
        private Context mContext;
        private CombinedData mPriceChartData;
        private ChartType mType;
        private LineData mLineData = null;
        private CandleData mCandleData = null;

        public ChartLayer(ChartType type)
        {
            mType = type;
        }

        public void init(Context context, CombinedData priceChartData, CombinedData volumeChartData)
        {
            mContext = context;
            mPriceChartData = priceChartData;
        }

        public void onQuoteResults(Stock.IQuoteCollection stockHistory, Stock.QuoteCollectionContext context)
        {
            Stock.IQuote initialQuote = stockHistory.get(0);
            Stock.IQuote finalQuote = stockHistory.get(stockHistory.size() -1);

            if (mType == ChartType.Line) {

                ArrayList<ILineDataSet> priceDataSets = new ArrayList<ILineDataSet>();
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
                lineSet.setLineWidth(1f);
                lineSet.setValueTextSize(9f);
                lineSet.setDrawFilled(true);
                lineSet.setFormLineWidth(1f);
                lineSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
                lineSet.setFormSize(15.f);
                lineSet.setColor(ContextCompat.getColor(mContext, R.color.colorSecondary));
                lineSet.setFillColor(ContextCompat.getColor(mContext, R.color.colorSecondary));
                lineSet.setDrawCircles(false);
                lineSet.setDrawValues(false);
                priceDataSets.add(lineSet);

                mPriceChartData.setData(new LineData(priceDataSets));
            }
            else
            {
                // candle coming soon!
            }
        }
    }
}

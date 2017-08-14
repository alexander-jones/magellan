package com.magellan.magellan;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class StockPriceMetric {

    public static enum ChartType
    {
        Candle,
        Line
    }

    public static class BasicChartLayer implements Metric.IChartLayer
    {
        private Context mContext;
        private CombinedData mPriceChartData;
        private ChartType mType;
        private LineData mLineData = null;
        private CandleData mCandleData = null;

        public BasicChartLayer(ChartType type)
        {
            mType = type;
        }

        public void init(Context context, CombinedData priceChartData)
        {
            mContext = context;
            mPriceChartData = priceChartData;
        }

        public void onQuoteResults(Stock.IQuoteCollection stockHistory, Stock.QuoteCollectionContext context)
        {
            Stock.IQuote initialQuote = stockHistory.get(0);
            Stock.IQuote finalQuote = stockHistory.get(stockHistory.size() -1);

            if (mType == ChartType.Line) {
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

                ArrayList<ILineDataSet> priceDataSets = new ArrayList<ILineDataSet>();
                priceDataSets.add(lineSet);
                mPriceChartData.setData(new LineData(priceDataSets));
            }
            else
            {
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
                candleSet.setShadowColor(Color.DKGRAY);
                candleSet.setShadowWidth(0.7f);
                candleSet.setDecreasingColor(ContextCompat.getColor(mContext, R.color.colorPriceDown));
                candleSet.setDecreasingPaintStyle(Paint.Style.FILL);
                candleSet.setIncreasingColor(ContextCompat.getColor(mContext, R.color.colorPriceUp));
                candleSet.setIncreasingPaintStyle(Paint.Style.STROKE);
                candleSet.setNeutralColor(Color.BLUE);
                candleSet.setDrawIcons(false);
                candleSet.setHighlightEnabled(true);
                candleSet.enableDashedHighlightLine(10f, 5f, 0f);
                candleSet.setHighLightColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                candleSet.setValueTextSize(9f);
                candleSet.setFormLineWidth(1f);
                candleSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
                candleSet.setFormSize(15.f);
                candleSet.setColor(ContextCompat.getColor(mContext, R.color.colorSecondary));
                candleSet.setDrawValues(false);

                ArrayList<ICandleDataSet> priceDataSets = new ArrayList<ICandleDataSet>();
                priceDataSets.add(candleSet);
                mPriceChartData.setData(new CandleData(priceDataSets));
            }
        }
    }
}

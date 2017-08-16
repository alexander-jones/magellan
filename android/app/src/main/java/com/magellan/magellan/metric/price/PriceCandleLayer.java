package com.magellan.magellan.metric.price;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;

import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.magellan.magellan.R;
import com.magellan.magellan.metric.IMetricLayer;
import com.magellan.magellan.quote.IQuote;

import java.util.ArrayList;
import java.util.List;

public class PriceCandleLayer implements IMetricLayer
{
    private Context mContext;

    public PriceCandleLayer(Context context)
    {
        mContext = context;
    }

    public void onDrawQuotes(List<IQuote> quotes, int missingStartSteps, int missingEndSteps, CombinedData chartData)
    {
        IQuote initialQuote = quotes.get(0);
        IQuote finalQuote = quotes.get(quotes.size() - 1);

        ArrayList<CandleEntry> priceValues = new ArrayList<CandleEntry>();

        for (int j = 0; j < missingStartSteps; ++j)
            priceValues.add(new CandleEntry(j, initialQuote.getHigh(), initialQuote.getLow(), initialQuote.getOpen(), initialQuote.getClose(), null));

        for (int j = missingStartSteps; j < quotes.size() + missingStartSteps; ++j) {
            IQuote quote = quotes.get(j - missingStartSteps);
            priceValues.add(new CandleEntry(j, quote.getHigh(), quote.getLow(), quote.getOpen(), quote.getClose(), quote));
        }

        for (int j = missingStartSteps + quotes.size(); j < quotes.size() + missingStartSteps + missingEndSteps; ++j)
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

        CandleData data = chartData.getCandleData();
        if (data == null) {
            ArrayList<ICandleDataSet> priceDataSets = new ArrayList<ICandleDataSet>();
            priceDataSets.add(candleSet);
            data = new CandleData(priceDataSets);
        } else
            data.addDataSet(candleSet);
        chartData.setData(data);
    }

    public String getName()
    {
        return "Candle Chart";
    }

    public String getShortName()
    {
        return "CAN";
    }
}

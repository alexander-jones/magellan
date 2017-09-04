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
import com.magellan.magellan.quote.Quote;

import java.util.ArrayList;
import java.util.List;

public class PriceCandleLayer implements IMetricLayer
{
    private Context mContext;

    public PriceCandleLayer(Context context)
    {
        mContext = context;
    }

    public void onDrawQuotes(List<Quote> quotes, int missingStartSteps, int missingEndSteps, CombinedData chartData)
    {
        Quote initialQuote = quotes.get(0);
        Quote finalQuote = quotes.get(quotes.size() - 1);

        ArrayList<CandleEntry> priceValues = new ArrayList<CandleEntry>();

        for (int j = missingStartSteps; j < quotes.size() + missingStartSteps; ++j) {
            Quote quote = quotes.get(j - missingStartSteps);
            priceValues.add(new CandleEntry(j, quote.high, quote.low, quote.open, quote.close, quote));
        }

        CandleDataSet missingDataSet = null;
        if (missingStartSteps > 0 || missingEndSteps > 0)
        {
            ArrayList<CandleEntry> missingPriceValues = new ArrayList<CandleEntry>();

            for (int j = 0; j < missingStartSteps; ++j)
                missingPriceValues.add(new CandleEntry(j, initialQuote.high, initialQuote.low, initialQuote.open, initialQuote.close, null));

            for (int j = missingStartSteps + quotes.size(); j < quotes.size() + missingStartSteps + missingEndSteps; ++j)
                missingPriceValues.add(new CandleEntry(j, finalQuote.high, finalQuote.low, finalQuote.open, finalQuote.close, null));

            missingDataSet = new CandleDataSet(missingPriceValues, "");
            missingDataSet.setDrawIcons(false);
            missingDataSet.setHighlightEnabled(false);
            missingDataSet.setHighLightColor(Color.TRANSPARENT);
            missingDataSet.setColor(Color.TRANSPARENT);
            missingDataSet.setDecreasingColor(Color.TRANSPARENT);
            missingDataSet.setIncreasingColor(Color.TRANSPARENT);
            missingDataSet.setHighLightColor(Color.TRANSPARENT);
            missingDataSet.setColor(Color.TRANSPARENT);
            missingDataSet.setShadowColor(Color.TRANSPARENT);
            missingDataSet.setNeutralColor(Color.TRANSPARENT);
            missingDataSet.setDrawValues(false);
        }

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
            if (missingDataSet != null)
                priceDataSets.add(missingDataSet);
            data = new CandleData(priceDataSets);
        } else {
            data.addDataSet(candleSet);
            if (missingDataSet != null)
                data.addDataSet(missingDataSet);
        }
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

package com.magellan.magellan;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.magellan.magellan.equity.Equity;
import com.magellan.magellan.metric.price.PriceLineLayer;
import com.magellan.magellan.metric.price.PriceMetric;
import com.magellan.magellan.quote.Quote;
import com.magellan.magellan.quote.QuoteQuery;

import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

public class WatchListRowAdapter extends RecyclerView.Adapter<WatchListRowAdapter.ViewHolder>
{
    public static class DataHolder
    {
        DataHolder(Equity e)
        {
            equity = e;
            quotes = null;
            query = null;
        }

        public Equity equity;
        public List<Quote> quotes;
        public QuoteQuery query;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public View rootView;
        public TextView symbol;
        public TextView value;
        public ProgressBar progress;
        public CombinedChart chart;
        public CombinedData allData;
        public PriceLineLayer priceLineLayer;

        public ViewHolder(View v) {
            super(v);
            allData = new CombinedData();
            priceLineLayer = new PriceLineLayer(mPriceUpLineStyler);
            symbol = (TextView)v.findViewById(R.id.symbol);
            value =(TextView) v.findViewById(R.id.value);
            chart = (CombinedChart)v.findViewById(R.id.chart);
            progress = (ProgressBar)v.findViewById(R.id.progress);
            rootView = v;
            ApplicationContext.initializeSimpleChart(v.getContext(), chart);

        }
    }

    private SolidLineDataSetStyler mPriceUpLineStyler;
    private SolidLineDataSetStyler mPriceDownLineStyler;
    private AdapterView.OnClickListener mListener;
    private List<DataHolder> mData;
    private Context mContext;
    public WatchListRowAdapter(Context context, List<DataHolder> data, AdapterView.OnClickListener listener) {
        mContext = context;
        mData = data;
        mListener = listener;
        mPriceUpLineStyler = new SolidLineDataSetStyler(ContextCompat.getColor(mContext, R.color.colorPriceUp));
        mPriceDownLineStyler = new SolidLineDataSetStyler(ContextCompat.getColor(mContext, R.color.colorPriceDown));
    }

    @Override
    public WatchListRowAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.watctlist_stock_row, parent, false);
        ViewHolder vh = new ViewHolder(v);
        v.setOnClickListener(mListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        DataHolder dh = mData.get(position);
        vh.symbol.setText(dh.equity.getSymbol());
        vh.rootView.setTag(dh);

        if (dh.quotes != null && !dh.quotes.isEmpty())
        {
            vh.progress.setVisibility(View.GONE);
            Quote initialQuote = dh.quotes.get(0);
            Quote finalQuote = dh.quotes.get(dh.quotes.size() -1);

            Duration missingStartDuration = new Duration(dh.query.start, initialQuote.time);
            Duration missingEndDuration = new Duration(finalQuote.time, dh.query.end);

            Duration intervalDuration = dh.query.getIntervalAsDuration();
            int missingStartSteps = (int)(missingStartDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());
            int missingEndSteps = (int)(missingEndDuration.getStandardMinutes() / intervalDuration.getStandardMinutes());

            float startingOpen = initialQuote.open;
            if (vh.allData.getLineData() != null)
                vh.allData.getLineData().clearValues();

            vh.value.setText(PriceMetric.valueToString(finalQuote.close));
            if (finalQuote.close > initialQuote.open) {
                vh.priceLineLayer.setStyler(mPriceUpLineStyler);
                vh.value.setTextColor(ContextCompat.getColor(mContext, R.color.colorPriceUp));
            }
            else {
                vh.priceLineLayer.setStyler(mPriceDownLineStyler);
                vh.value.setTextColor(ContextCompat.getColor(mContext, R.color.colorPriceDown));
            }

            // draw center line
            ArrayList<Entry> centerLineValues = new ArrayList<Entry>();
            centerLineValues.add(new Entry(0, startingOpen, null));
            centerLineValues.add(new Entry(dh.quotes.size() + missingStartSteps + missingEndSteps - 1, startingOpen, null));

            LineDataSet centerLineSet = new LineDataSet(centerLineValues, "");
            centerLineSet.setDrawIcons(false);
            centerLineSet.setHighlightEnabled(false);
            centerLineSet.enableDashedLine(10f, 10f, 0f);
            centerLineSet.setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            centerLineSet.setFillColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            centerLineSet.setLineWidth(1f);
            centerLineSet.setDrawCircles(false);
            centerLineSet.setDrawValues(false);

            LineData data = vh.allData.getLineData();
            if (data == null){
                ArrayList<ILineDataSet> priceDataSets = new ArrayList<ILineDataSet>();
                priceDataSets.add(centerLineSet);
                data = new LineData(priceDataSets);
            }
            else
                data.addDataSet(centerLineSet);
            vh.allData.setData(data);

            vh.priceLineLayer.onDrawQuotes(dh.quotes, 0, 0, vh.allData); // don't draw missing start / end steps when center line will pad data for us

            float lowestPrice = Float.MAX_VALUE;
            float highestPrice = -Float.MAX_VALUE;
            for (Quote q : dh.quotes)
            {
                if (lowestPrice > q.low)
                    lowestPrice = q.low;

                if (highestPrice < q.high)
                    highestPrice = q.high;
            }

            float fromCenterToExtent = Math.max(highestPrice - startingOpen, startingOpen - lowestPrice);
            vh.chart.getAxisLeft().setAxisMaximum(startingOpen + fromCenterToExtent);
            vh.chart.getAxisLeft().setAxisMinimum(startingOpen - fromCenterToExtent);
            vh.chart.setData(vh.allData);
            vh.chart.notifyDataSetChanged();
            vh.chart.fitScreen();
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}

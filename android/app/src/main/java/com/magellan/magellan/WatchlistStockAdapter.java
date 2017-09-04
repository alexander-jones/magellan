package com.magellan.magellan.metric;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.magellan.magellan.ApplicationContext;
import com.magellan.magellan.ChartGestureHandler;
import com.magellan.magellan.R;
import com.magellan.magellan.stock.Stock;

import java.util.List;

public class WatchlistStockAdapter extends RecyclerView.Adapter<WatchlistStockAdapter.ViewHolder>
{
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public View rootView;
        public TextView symbol;
        public TextView price;
        public TextView volume;
        public CombinedChart chart;

        public ViewHolder(View v, AdapterView.OnClickListener listener) {
            super(v);
            symbol = (TextView)v.findViewById(R.id.symbol);
            price =(TextView) v.findViewById(R.id.price);
            volume = (TextView)v.findViewById(R.id.volume);
            chart = (CombinedChart)v.findViewById(R.id.chart);
            rootView = v;
            v.setOnClickListener(listener);
            ApplicationContext.initializeSimpleChart(chart);
        }

    }

    private AdapterView.OnClickListener mListener;
    private List<Stock> mStocks;
    public WatchlistStockAdapter(List<Stock> stocks, AdapterView.OnClickListener listener) {
        mStocks = stocks;
        mListener = listener;
    }

    @Override
    public WatchlistStockAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,  int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.watctlist_stock_row, parent, false);
        ViewHolder vh = new ViewHolder(v, mListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Stock stock = mStocks.get(position);
        holder.symbol.setText(stock.getSymbol());
        holder.rootView.setTag(stock);
    }

    @Override
    public int getItemCount() {
        return mStocks.size();
    }
}

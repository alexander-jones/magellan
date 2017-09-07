package com.magellan.magellan;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.magellan.magellan.equity.Equity;

import java.util.List;

public class WatchlistStockAdapter extends RecyclerView.Adapter<WatchlistStockAdapter.ViewHolder>
{
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public View rootView;
        public TextView symbol;
        public TextView value;
        public TextView volume;
        public CombinedChart chart;

        public ViewHolder(View v, AdapterView.OnClickListener listener) {
            super(v);
            symbol = (TextView)v.findViewById(R.id.symbol);
            value =(TextView) v.findViewById(R.id.value);
            chart = (CombinedChart)v.findViewById(R.id.chart);
            rootView = v;
            v.setOnClickListener(listener);
            ApplicationContext.initializeSimpleChart(v.getContext(), chart);
        }

    }

    private AdapterView.OnClickListener mListener;
    private List<Equity> mEquities;
    public WatchlistStockAdapter(List<Equity> equities, AdapterView.OnClickListener listener) {
        mEquities = equities;
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
        Equity equity = mEquities.get(position);
        holder.symbol.setText(equity.getSymbol());
        holder.rootView.setTag(equity);
    }

    @Override
    public int getItemCount() {
        return mEquities.size();
    }
}

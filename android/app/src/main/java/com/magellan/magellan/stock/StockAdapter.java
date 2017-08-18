package com.magellan.magellan.stock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.magellan.magellan.R;
import com.magellan.magellan.stock.IStock;

import java.util.ArrayList;
import java.util.List;

public class StockAdapter extends ArrayAdapter<IStock>
{
    private final List<IStock> mItems;
    private final Context mContext;
    private final LayoutInflater mInflater;

    public StockAdapter (Context context, List<IStock> items)
    {
        super(context, R.layout.stock_row);

        mItems = items;
        mContext = context;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.stock_row, null);
            holder = new ViewHolder();
            holder.symbol = (TextView)convertView.findViewById(R.id.symbol);
            holder.company = (TextView) convertView.findViewById(R.id.company);
            holder.addToWatchList = (ImageButton)convertView.findViewById(R.id.add_to_watchlist);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }

        IStock stock = getItem(position);
        holder.symbol.setText(stock.getSymbol());
        holder.company.setText(stock.getCompany());
        return convertView;
    }

    @Override
    public int getCount()
    {
        return mItems.size();
    }

    @Override
    public IStock getItem(int position)
    {
        return mItems.get(position);
    }

    public class ViewHolder
    {
        TextView    symbol;
        TextView    company;
        ImageButton addToWatchList;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }
}
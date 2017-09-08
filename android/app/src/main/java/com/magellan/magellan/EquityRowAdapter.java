package com.magellan.magellan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.magellan.magellan.equity.Equity;

import java.util.List;

public abstract class EquityRowAdapter extends ArrayAdapter<Equity> implements View.OnClickListener
{
    private final List<Equity> mItems;
    private final Context mContext;
    private final LayoutInflater mInflater;

    public EquityRowAdapter(Context context, List<Equity> items)
    {
        super(context, R.layout.equity_row);

        mItems = items;
        mContext = context;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public abstract void onChangeStatusPressed(Equity equity, ViewHolder view);

    public abstract void onInitialStatus(Equity equity, ViewHolder view);

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        Equity equity = getItem(position);
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.equity_row, null);

        holder = new ViewHolder();
        holder.view = convertView;
        holder.symbol = (TextView)convertView.findViewById(R.id.symbol);
        holder.name = (TextView) convertView.findViewById(R.id.name);
        holder.changeStatus = (ImageButton)convertView.findViewById(R.id.change_status);
        holder.changeStatus.setOnClickListener(this);
        holder.changeStatus.setTag(holder);
        convertView.setTag(equity);

        onInitialStatus(equity, holder);

        holder.symbol.setText(equity.getSymbol());
        holder.name.setText(equity.getName());

        return convertView;
    }

    @Override
    public int getCount()
    {
        return mItems.size();
    }

    @Override
    public Equity getItem(int position)
    {
        return mItems.get(position);
    }

    @Override
    public void onClick(View view) {
        ViewHolder holder = (ViewHolder)view.getTag();
        Equity equity = (Equity)holder.view.getTag();
        onChangeStatusPressed(equity, holder);
    }

    public class ViewHolder
    {
        View        view;
        TextView    symbol;
        TextView    name;
        ImageButton changeStatus;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }
}
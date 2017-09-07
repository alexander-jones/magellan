package com.magellan.magellan.equity;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.magellan.magellan.ApplicationContext;
import com.magellan.magellan.R;

import java.util.List;

public class EquityAdapter extends ArrayAdapter<Equity> implements View.OnClickListener
{
    private final List<Equity> mItems;
    private final Context mContext;
    private final LayoutInflater mInflater;

    public EquityAdapter(Context context, List<Equity> items)
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
        Equity equity = getItem(position);
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.stock_row, null);
            holder = new ViewHolder();
            holder.symbol = (TextView)convertView.findViewById(R.id.symbol);
            holder.company = (TextView) convertView.findViewById(R.id.company);
            holder.changeWatchListStatus = (ImageButton)convertView.findViewById(R.id.change_watchlist_status);
            holder.changeWatchListStatus.setOnClickListener(this);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.changeWatchListStatus.setTag(equity);

        if (ApplicationContext.getWatchListIndex(equity) == -1)
            holder.changeWatchListStatus.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add_24dp));
        else
            holder.changeWatchListStatus.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_remove_24dp));

        holder.symbol.setText(equity.getSymbol());
        holder.company.setText(equity.getName());

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
        ImageButton button = (ImageButton)view;
        Equity equity = (Equity)view.getTag();
        int curStockIndex = ApplicationContext.getWatchListIndex(equity);
        if (curStockIndex == -1)
        {
            if (ApplicationContext.addToWatchList(equity))
                button.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_remove_24dp));
        }
        else
        {
            if (ApplicationContext.removeFromWatchList(curStockIndex))
                button.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add_24dp));
        }
    }

    public class ViewHolder
    {
        TextView    symbol;
        TextView    company;
        ImageButton changeWatchListStatus;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }
}
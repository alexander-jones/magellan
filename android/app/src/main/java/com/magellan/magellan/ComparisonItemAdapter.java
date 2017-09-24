package com.magellan.magellan;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public abstract class ComparisonItemAdapter extends RecyclerView.Adapter<ComparisonItemAdapter.ViewHolder> implements View.OnClickListener
{
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView button;
        public TextView value;
        public ViewHolder(View root, View.OnClickListener list) {
            super(root);
            button = (TextView)root.findViewById(R.id.button);
            value = (TextView)root.findViewById(R.id.value);
            button.setOnClickListener(list);
        }
    }


    private List<String> mValueTexts = null;
    private List<ComparisonList.Item> mItems;

    public ComparisonItemAdapter(List<ComparisonList.Item> items, List<String> texts)
    {
        mValueTexts = texts;
        mItems = items;
    }

    @Override
    public ComparisonItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.comparison_item, parent, false);;
        ViewHolder vh = new ViewHolder(root, this);
        root.setTag(vh);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ComparisonList.Item item = mItems.get(position);
        holder.button.setTag(item);
        holder.button.setText(item.equity.getSymbol());
        holder.value.setText(mValueTexts.get(position));
        if (item.enabled)
        {
            holder.button.setTextColor(item.color);
            holder.value.setTextColor(item.color);
        }
        else
        {
            Context context = holder.button.getContext();
            int color =  ContextCompat.getColor(context, R.color.colorBackgroundDark);
            holder.button.setTextColor(color);
            holder.value.setTextColor(color);
        }
    }

    @Override
    public int getItemCount()
    {
        return mItems.size();
    }

    @Override
    public void onClick(View view) {
        onItemSelected((ComparisonList.Item)view.getTag());
    }

    public abstract void onItemSelected(ComparisonList.Item item);
}

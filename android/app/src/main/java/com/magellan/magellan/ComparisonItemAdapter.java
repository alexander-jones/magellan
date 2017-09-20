package com.magellan.magellan;

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


    private List<Integer> mColors;
    private List<String> mValueTexts;
    private List<String> mButtonTexts;

    public ComparisonItemAdapter(List<String> labels, List<String> texts)
    {
        mButtonTexts = labels;
        mValueTexts = texts;
    }

    public ComparisonItemAdapter(List<String> labels, List<String> texts, List<Integer> colors)
    {
        mButtonTexts = labels;
        mValueTexts = texts;
        mColors = colors;
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
        holder.button.setText(mButtonTexts.get(position));
        holder.value.setText(mValueTexts.get(position));
        if (mColors != null)
        {
            int color = mColors.get(position);
            holder.button.setTextColor(color);
            holder.value.setTextColor(color);
        }
    }

    @Override
    public int getItemCount()
    {
        return mButtonTexts.size();
    }

    @Override
    public void onClick(View view) {
        onButtonPressed((TextView)view);
    }

    public abstract void onButtonPressed(TextView textView);
}

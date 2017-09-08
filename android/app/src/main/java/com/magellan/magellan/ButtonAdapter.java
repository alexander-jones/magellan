package com.magellan.magellan;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.magellan.magellan.equity.Equity;

import java.util.List;

public abstract class ButtonAdapter extends RecyclerView.Adapter<ButtonAdapter.ViewHolder> implements View.OnClickListener
{
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Button button;
        public ViewHolder(Button v) {
            super(v);
            button = v;
        }
        }


    private List<Integer> mTextColors;
    private List<String> mButtonTexts;
    public ButtonAdapter(List<String> labels)
    {
        mButtonTexts = labels;
    }
    public ButtonAdapter(List<String> labels, List<Integer> labelColors)
    {
        mButtonTexts = labels;
        mTextColors = labelColors;
    }

    @Override
    public ButtonAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Button v = (Button) LayoutInflater.from(parent.getContext()).inflate(R.layout.overlay_button, parent, false);
        v.setOnClickListener(this);
        ViewHolder vh = new ViewHolder(v);
        v.setTag(vh);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.button.setText(mButtonTexts.get(position));
        if (mTextColors != null)
            holder.button.setTextColor(mTextColors.get(position));
    }

    @Override
    public void onClick(View view) {
        Button button = (Button)view;
        onButtonPressed(button);
    }

    public abstract void onButtonPressed(Button button);

    @Override
    public int getItemCount()
    {
        return mButtonTexts.size();
    }
}

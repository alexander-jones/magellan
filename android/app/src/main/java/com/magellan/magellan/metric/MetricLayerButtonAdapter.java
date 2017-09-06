package com.magellan.magellan.metric;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import com.magellan.magellan.R;

import java.util.List;

public class MetricLayerButtonAdapter extends RecyclerView.Adapter<MetricLayerButtonAdapter.ViewHolder>
{
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Button button;
        public ViewHolder(Button v) {
            super(v);
            button = v;
        }
    }


    private List<Integer> mActiveLayerColors;
    private List<String> mActiveLayerLabels;
    public MetricLayerButtonAdapter(List<String> labels)
    {
        mActiveLayerLabels = labels;
    }
    public MetricLayerButtonAdapter(List<String> labels, List<Integer> labelColors)
    {
        mActiveLayerLabels = labels;
        mActiveLayerColors = labelColors;
    }

    @Override
    public MetricLayerButtonAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Button v = (Button) LayoutInflater.from(parent.getContext()).inflate(R.layout.overlay_button, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.button.setText(mActiveLayerLabels.get(position));
        if (mActiveLayerColors != null)
            holder.button.setTextColor(mActiveLayerColors.get(position));
    }

    @Override
    public int getItemCount() {
        return mActiveLayerLabels.size();
    }
}

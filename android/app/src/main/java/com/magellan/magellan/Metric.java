package com.magellan.magellan;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.mikephil.charting.data.CombinedData;

import java.util.List;

public class Metric {
    public static interface IChartLayer {
        public void init(Context context, CombinedData priceChartData);

        public void onQuoteResults(Stock.IQuoteCollection quotes, Stock.QuoteCollectionContext context);
    }

    public static class LayerAdapter extends RecyclerView.Adapter<LayerAdapter.ViewHolder>
    {

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public Button button;
            public ViewHolder(Button v) {
                super(v);
                button = v;
            }
        }


        int mTextColor;
        int mColor;
        private List<String> mActiveLayerLabels;
        // Provide a suitable constructor (depends on the kind of dataset)
        public LayerAdapter(List<String> labels, int color, int textColor) {
            mActiveLayerLabels = labels;
            mColor = color;
            mTextColor = textColor;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public LayerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
            // create a new view
            Button v = (Button) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.overlay_button, parent, false);
            v.setBackgroundColor(mColor);
            v.setTextColor(mTextColor);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.button.setText(mActiveLayerLabels.get(position));

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mActiveLayerLabels.size();
        }
    }

}

package com.magellan.magellan;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

class ActiveOverlayAdapter extends RecyclerView.Adapter<ActiveOverlayAdapter.ViewHolder>
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


    private List<String> mActiveOverlays;
    // Provide a suitable constructor (depends on the kind of dataset)
    public ActiveOverlayAdapter() {
        mActiveOverlays = new ArrayList<String>();
        mActiveOverlays.add("SP");
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ActiveOverlayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        // create a new view
        Button v = (Button) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.overlay_button, parent, false);
        v.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.colorSecondary));
        v.setTextColor(Color.WHITE);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.button.setText(mActiveOverlays.get(position));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mActiveOverlays.size();
    }
}

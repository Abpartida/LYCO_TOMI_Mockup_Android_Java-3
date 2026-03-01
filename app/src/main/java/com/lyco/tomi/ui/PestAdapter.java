package com.lyco.tomi.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lyco.tomi.R;

import java.util.List;

public class PestAdapter extends RecyclerView.Adapter<PestAdapter.VH> {
    private List<PestItem> data;
    public PestAdapter(List<PestItem> d) { data = d; }

    public void setData(List<PestItem> d) {
        data = d;
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pest, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        PestItem it = data.get(pos);
        h.name.setText(it.name);
        h.count.setText(String.valueOf(it.count));
        h.time.setText(it.time);
        h.location.setText(it.location);
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, count, time, location;
        VH(View v) {
            super(v);
            name = v.findViewById(R.id.tvPestName);
            count = v.findViewById(R.id.tvPestCount);
            time = v.findViewById(R.id.tvPestTime);
            location = v.findViewById(R.id.tvPestLocation);
        }
    }
}

package com.lyco.tomi.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lyco.tomi.R;

import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.VH> {
    private final List<AlertItem> data;
    public AlertAdapter(List<AlertItem> d) { data = d; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        AlertItem it = data.get(pos);
        h.t.setText(it.title);
        h.d.setText(it.desc);
        h.tm.setText(it.time);
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView t, d, tm;
        VH(View v) {
            super(v);
            t = v.findViewById(R.id.tvAlertTitle);
            d = v.findViewById(R.id.tvAlertDesc);
            tm = v.findViewById(R.id.tvAlertTime);
        }
    }
}

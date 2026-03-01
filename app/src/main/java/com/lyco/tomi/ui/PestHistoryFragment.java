package com.lyco.tomi.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lyco.tomi.R;
import com.lyco.tomi.data.PestDbHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PestHistoryFragment extends Fragment {

    private List<PestItem> all = new ArrayList<>();
    private PestAdapter adapter;
    private PestDbHelper dbHelper;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new PestDbHelper(requireContext().getApplicationContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pest_history, container, false);
        RecyclerView rv = v.findViewById(R.id.rvPests);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PestAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        loadPests();

        EditText search = v.findViewById(R.id.etSearchPest);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String q = s.toString().toLowerCase();
                List<PestItem> filtered = all.stream()
                    .filter(p -> p.name.toLowerCase().contains(q))
                    .collect(Collectors.toList());
                adapter.setData(filtered);
            }
        });

        return v;
    }

    private void loadPests() {
        ioExecutor.execute(() -> {
            List<PestItem> pests = dbHelper.getAllPests();
            all = pests;
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(() -> adapter.setData(new ArrayList<>(all)));
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdownNow();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}

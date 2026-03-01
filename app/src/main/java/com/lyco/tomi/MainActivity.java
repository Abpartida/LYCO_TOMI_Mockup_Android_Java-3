package com.lyco.tomi;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lyco.tomi.ui.AlertHistoryFragment;
import com.lyco.tomi.ui.ControlFragment;
import com.lyco.tomi.ui.PestHistoryFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottom = findViewById(R.id.bottom_nav);
        bottom.setOnItemSelectedListener(this::onNavItem);
        bottom.setSelectedItemId(R.id.nav_alerts); // Default tab asdsada
    }

    private boolean onNavItem(@NonNull MenuItem item) {
        Fragment f;
        int id = item.getItemId();
        if (id == R.id.nav_alerts) f = new AlertHistoryFragment();
        else if (id == R.id.nav_pests) f = new PestHistoryFragment();
        else f = new ControlFragment();

        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, f)
            .commit();
        return true;
    }
}

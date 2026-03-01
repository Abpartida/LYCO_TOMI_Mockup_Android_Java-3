package com.lyco.tomi.ui;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lyco.tomi.R;

import java.util.ArrayList;
import java.util.List;

public class AlertHistoryFragment extends Fragment {

    private static final String ALERT_CHANNEL_ID = "lyco_alerts";
    private static final int TEST_NOTIFICATION_ID = 42;
    private static final int REQUEST_POST_NOTIFICATIONS = 500;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_alert_history, container, false);
        RecyclerView rv = v.findViewById(R.id.rvAlerts);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new AlertAdapter(mockAlerts()));

        Button test = v.findViewById(R.id.btnTestNotification);
        test.setOnClickListener(btn -> sendTestNotification());
        createNotificationChannel();
        return v;
    }

    private List<AlertItem> mockAlerts() {
        List<AlertItem> list = new ArrayList<>();
        list.add(new AlertItem("Pest Detected", "Whiteflies detected on Row 3", "2025-10-06 13:22"));
        list.add(new AlertItem("Low Battery", "Robot battery under 20%", "2025-10-06 12:05"));
        list.add(new AlertItem("Path Complete", "Completed Route A", "2025-10-05 17:44"));
        return list;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Context ctx = getContext();
            if (ctx == null) return;
            NotificationManager manager = ctx.getSystemService(NotificationManager.class);
            if (manager == null) return;
            NotificationChannel channel = new NotificationChannel(
                ALERT_CHANNEL_ID,
                getString(R.string.alert_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(getString(R.string.alert_history));
            manager.createNotificationChannel(channel);
        }
    }

    private void sendTestNotification() {
        Context ctx = getContext();
        if (ctx == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
            Toast.makeText(ctx, R.string.alert_permission_needed, Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, ALERT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(getString(R.string.alert_test_title))
            .setContentText(getString(R.string.alert_test_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true);

        NotificationManagerCompat.from(ctx).notify(TEST_NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_POST_NOTIFICATIONS &&
            grantResults.length > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendTestNotification();
        }
    }
}

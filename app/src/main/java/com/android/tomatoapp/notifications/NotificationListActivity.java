package com.android.tomatoapp.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.util.Log;

import com.android.tomatoapp.R;
import com.android.tomatoapp.common.managers.AppNotificationManager;
import com.android.tomatoapp.core.ui.BaseBottomNavActivity;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import java.util.Collections;
import java.util.Iterator;

public class NotificationListActivity extends BaseBottomNavActivity {
    
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<AppNotificationManager.AppNotification> notificationList;
    private MaterialButton btnClearAll;
    private TextView emptyStateText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_notification_list);
            
            setupBottomNavigation();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing notifications screen", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notifications");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        recyclerView = findViewById(R.id.notificationRecyclerView);
        btnClearAll = findViewById(R.id.btnClearAll);
        emptyStateText = findViewById(R.id.emptyStateText);
        
        if (recyclerView == null) {
            Toast.makeText(this, "Error: Could not initialize notification list", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // Setup swipe to remove
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder,
                                  @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder target) {
                return false;
            }
            
            @Override
            public void onSwiped(@NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION || position < 0 || position >= notificationList.size()) {
                    return;
                }
                AppNotificationManager.AppNotification notification = notificationList.get(position);
                AppNotificationManager.removeNotification(NotificationListActivity.this, notification.id);
                notificationList.remove(position);
                adapter.notifyItemRemoved(position);
                updateEmptyState();
            }
        };
        
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        
        // Clear all button
        if (btnClearAll != null) {
            btnClearAll.setOnClickListener(v -> {
                if (notificationList.isEmpty()) {
                    Toast.makeText(this, "No notifications to clear", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                new AlertDialog.Builder(this)
                        .setTitle("Clear All Notifications")
                        .setMessage("Are you sure you want to clear all notifications?")
                        .setPositiveButton("Clear All", (dialog, which) -> {
                            AppNotificationManager.clearAllNotifications(this);
                            notificationList.clear();
                            adapter.notifyDataSetChanged();
                            updateEmptyState();
                            Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
        
        loadNotifications();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh notifications when returning to this screen
        loadNotifications();
    }
    
    private void loadNotifications() {
        try {
            if (notificationList == null) {
                notificationList = new ArrayList<>();
            }
            notificationList.clear();
            notificationList.addAll(AppNotificationManager.getNotifications(this));
            Collections.sort(notificationList, (a, b) -> Long.compare(b.timestamp, a.timestamp));
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            updateEmptyState();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading notifications", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateEmptyState() {
        if (notificationList == null) {
            return;
        }
        if (emptyStateText != null) {
            emptyStateText.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (btnClearAll != null) {
            btnClearAll.setEnabled(!notificationList.isEmpty());
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void navigateToNotification(AppNotificationManager.AppNotification notification) {
        if (notification.targetActivity == null || notification.targetActivity.isEmpty()) {
            Toast.makeText(this, "No target activity specified", Toast.LENGTH_SHORT).show();
            return;
        }

        Class<?> targetClass = resolveActivity(notification.targetActivity);
        if (targetClass == null) {
            Toast.makeText(this, "Target activity not found: " + notification.targetActivity,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, targetClass);

        if (notification.extraData != null && !notification.extraData.isEmpty()) {
            try {
                JSONObject extraData = new JSONObject(notification.extraData);
                Iterator<String> keys = extraData.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    intent.putExtra(key, extraData.optString(key));
                }
                // Workprogram expects "startDate"; fall back to "date" if missing
                if (!extraData.has("startDate") && extraData.has("date")) {
                    intent.putExtra("startDate", extraData.optString("date"));
                }
            } catch (Exception e) {
                Log.w("NotifNav", "Bad extraData", e);
            }
        }

        startActivity(intent);
        AppNotificationManager.removeNotification(this, notification.id);
        loadNotifications();
    }

    /** Finds the activity among those declared in the manifest, by full or simple name. */
    private Class<?> resolveActivity(String name) {
        try {
            ActivityInfo[] activities = getPackageManager()
                    .getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES).activities;
            if (activities == null) return null;
            String simple = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : name;
            String bySimpleName = null;
            for (ActivityInfo info : activities) {
                if (info.name.equals(name)) return Class.forName(info.name);
                if (bySimpleName == null && info.name.endsWith("." + simple)) {
                    bySimpleName = info.name;
                }
            }
            if (bySimpleName != null) return Class.forName(bySimpleName);
        } catch (Exception e) {
            Log.e("NotifNav", "Unresolved targetActivity: " + name, e);
        }
        return null;
    }
    
    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
        private List<AppNotificationManager.AppNotification> notifications;
        
        NotificationAdapter(List<AppNotificationManager.AppNotification> notifications) {
            this.notifications = notifications;
        }
        
        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new NotificationViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            if (position < 0 || position >= notifications.size()) {
                return;
            }
            AppNotificationManager.AppNotification notification = notifications.get(position);
            if (notification == null) {
                return;
            }
            
            try {
                holder.titleText.setText(notification.title != null ? notification.title : "Notification");
                holder.messageText.setText(notification.message != null ? notification.message : "");
                
                // Format timestamp
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                String timeStr = sdf.format(new Date(notification.timestamp));
                holder.timeText.setText(timeStr);
                
                // Set click listener
                holder.cardView.setOnClickListener(v -> {
                    navigateToNotification(notification);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public int getItemCount() {
            return notifications.size();
        }
        
        class NotificationViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardView;
            TextView titleText, messageText, timeText;
            
            NotificationViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.notificationCard);
                titleText = itemView.findViewById(R.id.notificationTitle);
                messageText = itemView.findViewById(R.id.notificationMessage);
                timeText = itemView.findViewById(R.id.notificationTime);
                
                // Ensure all views are found
                if (cardView == null || titleText == null || messageText == null || timeText == null) {
                    throw new IllegalStateException("Notification item layout is missing required views");
                }
            }
        }
    }
}

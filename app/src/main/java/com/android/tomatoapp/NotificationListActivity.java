package com.android.tomatoapp;

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

public class NotificationListActivity extends BaseDrawerActivity {
    
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
            
            setupDrawer();
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
        
        try {
            Class<?> targetClass = Class.forName("com.android.tomatoapp." + notification.targetActivity);
            Intent intent = new Intent(this, targetClass);
            
            // Parse extra data if available
            if (notification.extraData != null && !notification.extraData.isEmpty()) {
                try {
                    JSONObject extraData = new JSONObject(notification.extraData);
                    // Add common extras
                    if (extraData.has("programId")) {
                        intent.putExtra("programId", extraData.getString("programId"));
                    }
                    if (extraData.has("cultivar")) {
                        intent.putExtra("cultivar", extraData.getString("cultivar"));
                    }
                    if (extraData.has("date")) {
                        intent.putExtra("date", extraData.getString("date"));
                    }
                    if (extraData.has("detectionId")) {
                        intent.putExtra("detectionId", extraData.getString("detectionId"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            startActivity(intent);
            
            // Remove notification after navigating
            AppNotificationManager.removeNotification(this, notification.id);
            loadNotifications();
        } catch (ClassNotFoundException e) {
            Toast.makeText(this, "Target activity not found", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
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

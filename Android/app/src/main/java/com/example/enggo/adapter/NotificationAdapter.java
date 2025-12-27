package com.example.enggo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.enggo.R;
import com.example.enggo.model.Notification;
import com.example.enggo.user.NotificationDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notifications;
    private List<Notification> notificationsFiltered;

    public NotificationAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
        this.notificationsFiltered = new ArrayList<>(notifications);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationsFiltered.get(position);
        
        holder.tvType.setText(notification.getType());
        holder.tvTitle.setText(notification.getTitle());
        holder.tvContent.setText(notification.getPreviewContent());
        holder.tvTime.setText(notification.getCreatedAt());

        // Màu sắc theo type
        int typeColor = getTypeColor(notification.getType());
        holder.tvType.setTextColor(typeColor);

        // Highlight nếu chưa đọc
        if (!notification.isRead()) {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.notification_unread_bg)
            );
            holder.tvTitle.setTextColor(
                ContextCompat.getColor(context, R.color.text_primary)
            );
        } else {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.background_card)
            );
            holder.tvTitle.setTextColor(
                ContextCompat.getColor(context, R.color.text_secondary)
            );
        }

        // Click to view detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NotificationDetailActivity.class);
            intent.putExtra("NOTIFICATION_ID", notification.getId());
            intent.putExtra("NOTIFICATION_TYPE", notification.getType());
            intent.putExtra("NOTIFICATION_TITLE", notification.getTitle());
            intent.putExtra("NOTIFICATION_CONTENT", notification.getContent());
            intent.putExtra("NOTIFICATION_TIME", notification.getCreatedAt());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return notificationsFiltered.size();
    }

    private int getTypeColor(String type) {
        String normalizedType = type == null ? "" : type.trim().toLowerCase();
        switch (normalizedType) {
            case "event":
                return ContextCompat.getColor(context, R.color.notification_event);
            case "remind":
                return ContextCompat.getColor(context, R.color.notification_remind);
            case "warning":
                return ContextCompat.getColor(context, R.color.notification_warning);
            case "announcement":
                return ContextCompat.getColor(context, R.color.notification_announcement);
            default:
                return ContextCompat.getColor(context, R.color.text_secondary);
        }
    }

    // Filter method
    public void filter(String query) {
        notificationsFiltered.clear();
        if (query.isEmpty()) {
            notificationsFiltered.addAll(notifications);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Notification notification : notifications) {
                if (notification.getTitle().toLowerCase().contains(lowerQuery) ||
                    notification.getContent().toLowerCase().contains(lowerQuery)) {
                    notificationsFiltered.add(notification);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Update data
    public void updateData(List<Notification> newNotifications) {
        this.notifications.clear();
        this.notifications.addAll(newNotifications);
        this.notificationsFiltered.clear();
        this.notificationsFiltered.addAll(newNotifications);
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvType, tvTitle, tvContent, tvTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvType = itemView.findViewById(R.id.tvNotificationType);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvContent = itemView.findViewById(R.id.tvNotificationContent);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
        }
    }
}

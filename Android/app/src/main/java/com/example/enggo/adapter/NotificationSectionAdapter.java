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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final String[] TYPE_ORDER = {"event", "remind", "warning", "announcement"};

    private final Context context;
    private final List<Notification> notifications;
    private final List<Row> rows;
    private String query = "";

    public NotificationSectionAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = new ArrayList<>();
        this.rows = new ArrayList<>();
        updateData(notifications);
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).isHeader ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_notification_section, parent, false);
            return new HeaderViewHolder(view);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (row.isHeader) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvHeader.setText(row.header);
            return;
        }

        Notification notification = row.notification;
        ItemViewHolder itemHolder = (ItemViewHolder) holder;

        itemHolder.tvType.setText(notification.getType());
        itemHolder.tvTitle.setText(notification.getTitle());
        itemHolder.tvContent.setText(notification.getPreviewContent());
        itemHolder.tvTime.setText(notification.getCreatedAt());

        int typeColor = getTypeColor(notification.getType());
        itemHolder.tvType.setTextColor(typeColor);

        if (!notification.isRead()) {
            itemHolder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.notification_unread_bg)
            );
            itemHolder.tvTitle.setTextColor(
                ContextCompat.getColor(context, R.color.text_primary)
            );
        } else {
            itemHolder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.background_card)
            );
            itemHolder.tvTitle.setTextColor(
                ContextCompat.getColor(context, R.color.text_secondary)
            );
        }

        itemHolder.itemView.setOnClickListener(v -> {
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
        return rows.size();
    }

    public void filter(String query) {
        this.query = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        rebuildRows();
    }

    public void updateData(List<Notification> newNotifications) {
        notifications.clear();
        if (newNotifications != null) {
            notifications.addAll(newNotifications);
        }
        rebuildRows();
    }

    private void rebuildRows() {
        rows.clear();
        Map<String, List<Notification>> groups = new LinkedHashMap<>();
        for (String type : TYPE_ORDER) {
            groups.put(type, new ArrayList<>());
        }

        for (Notification notification : notifications) {
            if (!matchesQuery(notification)) {
                continue;
            }
            String normalizedType = normalizeType(notification.getType());
            List<Notification> list = groups.get(normalizedType);
            if (list == null) {
                list = groups.get("announcement");
            }
            if (list != null) {
                list.add(notification);
            }
        }

        for (String type : TYPE_ORDER) {
            List<Notification> list = groups.get(type);
            if (list == null || list.isEmpty()) {
                continue;
            }
            rows.add(Row.header(labelForType(type)));
            for (Notification notification : list) {
                rows.add(Row.item(notification));
            }
        }

        notifyDataSetChanged();
    }

    private boolean matchesQuery(Notification notification) {
        if (query.isEmpty()) {
            return true;
        }
        String title = notification.getTitle() == null ? "" : notification.getTitle().toLowerCase(Locale.ROOT);
        String content = notification.getContent() == null ? "" : notification.getContent().toLowerCase(Locale.ROOT);
        return title.contains(query) || content.contains(query);
    }

    private String normalizeType(String type) {
        if (type == null) {
            return "announcement";
        }
        String trimmed = type.trim().toLowerCase(Locale.ROOT);
        for (String allowed : TYPE_ORDER) {
            if (allowed.equals(trimmed)) {
                return allowed;
            }
        }
        return "announcement";
    }

    private String labelForType(String type) {
        switch (type) {
            case "event":
                return "Event";
            case "remind":
                return "Remind";
            case "warning":
                return "Warning";
            case "announcement":
            default:
                return "Announcement";
        }
    }

    private int getTypeColor(String type) {
        String normalizedType = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
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

    private static class Row {
        private final boolean isHeader;
        private final String header;
        private final Notification notification;

        private Row(boolean isHeader, String header, Notification notification) {
            this.isHeader = isHeader;
            this.header = header;
            this.notification = notification;
        }

        static Row header(String header) {
            return new Row(true, header, null);
        }

        static Row item(Notification notification) {
            return new Row(false, null, notification);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvNotificationSectionTitle);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvType, tvTitle, tvContent, tvTime;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvType = itemView.findViewById(R.id.tvNotificationType);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvContent = itemView.findViewById(R.id.tvNotificationContent);
            tvTime = itemView.findViewById(R.id.tvNotificationTime);
        }
    }
}

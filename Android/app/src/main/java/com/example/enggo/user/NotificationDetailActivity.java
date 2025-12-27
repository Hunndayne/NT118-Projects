package com.example.enggo.user;

import android.os.Bundle;
import android.widget.TextView;

import com.example.enggo.R;

public class NotificationDetailActivity extends BaseUserActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_detail);
        
        setupHeader();
        setupFooter();

        // Get data from intent
        String type = getIntent().getStringExtra("NOTIFICATION_TYPE");
        String title = getIntent().getStringExtra("NOTIFICATION_TITLE");
        String content = getIntent().getStringExtra("NOTIFICATION_CONTENT");
        String time = getIntent().getStringExtra("NOTIFICATION_TIME");

        // Setup views
        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());

        TextView tvType = findViewById(R.id.tvNotificationType);
        TextView tvTitle = findViewById(R.id.tvNotificationTitle);
        TextView tvContent = findViewById(R.id.tvNotificationContent);
        TextView tvTime = findViewById(R.id.tvNotificationTime);

        tvType.setText(type != null ? type : "");
        tvTitle.setText(title != null ? title : "");
        tvContent.setText(content != null ? content : "");
        tvTime.setText(time != null ? time : "");

        // TODO: Mark notification as read via API
        // markAsRead(notificationId);
    }
}

package com.example.enggo.teacher;

import com.example.enggo.R;
import com.example.enggo.adapter.NotificationAdapter;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.model.Notification;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationTeacherActivity extends BaseTeacherActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private EditText etSearch;
    private TextView tvBack;
    private FloatingActionButton fabSendNotification;
    private List<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_teacher);
        setupTeacherHeader();
        setupTeacherFooter();

        initViews();
        setupListeners();
        loadNotifications();
    }

    private void initViews() {
        tvBack = findViewById(R.id.tvBack);
        etSearch = findViewById(R.id.etSearchNotification);
        rvNotifications = findViewById(R.id.rvNotifications);
        fabSendNotification = findViewById(R.id.fabSendNotification);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(this, notificationList);
        rvNotifications.setAdapter(adapter);
    }

    private void setupListeners() {
        tvBack.setOnClickListener(v -> finish());

        fabSendNotification.setOnClickListener(v -> {
            Intent intent = new Intent(this, SendNotificationActivity.class);
            startActivity(intent);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadNotifications() {
        String token = getTokenFromDb();
        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getNotifications(token).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notificationList.clear();
                    notificationList.addAll(response.body());
                    adapter.updateData(notificationList);
                } else {
                    loadMockNotifications();
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                loadMockNotifications();
            }
        });
    }

    private void loadMockNotifications() {
        notificationList.clear();
        
        Notification notif1 = new Notification();
        notif1.setId(1L);
        notif1.setType("Event");
        notif1.setTitle("Welcome to EngGo!");
        notif1.setContent("Start your English learning journey today with interactive lessons.");
        notif1.setCreatedAt("2024-01-15 10:30:00");
        notif1.setRead(false);
        
        Notification notif2 = new Notification();
        notif2.setId(2L);
        notif2.setType("Remind");
        notif2.setTitle("Assignment Due Soon");
        notif2.setContent("Your English Essay assignment is due in 2 days. Don't forget to submit!");
        notif2.setCreatedAt("2024-01-14 14:20:00");
        notif2.setRead(false);
        
        Notification notif3 = new Notification();
        notif3.setId(3L);
        notif3.setType("Warning");
        notif3.setTitle("Account Security Alert");
        notif3.setContent("We detected a login from a new device. If this wasn't you, please secure your account.");
        notif3.setCreatedAt("2024-01-13 09:15:00");
        notif3.setRead(true);
        
        notificationList.add(notif1);
        notificationList.add(notif2);
        notificationList.add(notif3);
        
        adapter.updateData(notificationList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }
}

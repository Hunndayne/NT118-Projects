package com.example.enggo.user;

import com.example.enggo.R;
import com.example.enggo.adapter.NotificationAdapter;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.model.Notification;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationUserActivity extends BaseUserActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private EditText etSearch;
    private TextView tvBack;
    private List<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);
        setupHeader();
        setupFooter();

        initViews();
        setupListeners();
        loadNotifications();
    }

    private void initViews() {
        tvBack = findViewById(R.id.tvBack);
        etSearch = findViewById(R.id.etSearchNotification);
        rvNotifications = findViewById(R.id.rvNotifications);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(this, notificationList);
        rvNotifications.setAdapter(adapter);
    }

    private void setupListeners() {
        tvBack.setOnClickListener(v -> finish());

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
                    notificationList.clear();
                    adapter.updateData(notificationList);
                    Toast.makeText(NotificationUserActivity.this,
                            "Load notifications failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                notificationList.clear();
                adapter.updateData(notificationList);
                Toast.makeText(NotificationUserActivity.this,
                        "Cannot connect to server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

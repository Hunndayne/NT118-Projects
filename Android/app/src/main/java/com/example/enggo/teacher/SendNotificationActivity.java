package com.example.enggo.teacher;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.model.Notification;
import com.example.enggo.model.NotificationRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendNotificationActivity extends BaseTeacherActivity {

    private EditText etTitle, etContent;
    private Spinner spinnerType;
    private Button btnSend, btnCancel;
    private TextView tvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_notification);

        setupTeacherHeader();
        setupTeacherFooter();

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvBack = findViewById(R.id.tvBack);
        etTitle = findViewById(R.id.etNotificationTitle);
        etContent = findViewById(R.id.etNotificationContent);
        spinnerType = findViewById(R.id.spinnerNotificationType);
        btnSend = findViewById(R.id.btnSendNotification);
        btnCancel = findViewById(R.id.btnCancelNotification);

        // Setup spinner
        String[] types = {"Event", "Remind", "Warning", "Announcement"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_spinner_item, 
            types
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
    }

    private void setupListeners() {
        tvBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendNotification());
    }

    private void sendNotification() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        if (content.isEmpty()) {
            etContent.setError("Content is required");
            etContent.requestFocus();
            return;
        }

        String token = getTokenFromDb();
        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationRequest request = new NotificationRequest(type, title, content);
        
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.sendNotification(token, request).enqueue(new Callback<Notification>() {
            @Override
            public void onResponse(Call<Notification> call, Response<Notification> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SendNotificationActivity.this, 
                        "Notification sent successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "Failed to send notification";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg = "Error code: " + response.code();
                    }
                    Toast.makeText(SendNotificationActivity.this, 
                        errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Notification> call, Throwable t) {
                Toast.makeText(SendNotificationActivity.this, 
                    "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

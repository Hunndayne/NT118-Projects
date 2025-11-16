package com.example.enggo.user;
import com.example.enggo.R;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageButton; // Hoặc Button, tùy vào view của bạn
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public abstract class BaseUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Phương thức này sẽ được gọi trong onCreate của các Activity con
    // sau khi setContentView()
    protected void setupFooter() {
        LinearLayout btnHome = findViewById(R.id.btnHome); // Thay bằng ID thực tế
        LinearLayout btnCourse = findViewById(R.id.btnMyCourse);
        LinearLayout btnNotification = findViewById(R.id.btnNotification);
        LinearLayout btnMenu = findViewById(R.id.btnMenu);

        btnHome.setOnClickListener(v -> {
            // Không thực hiện hành động nếu đang ở HomeActivity
            if (!(this instanceof HomeUserActivity)) {
                Intent intent = new Intent(this, HomeUserActivity.class);
                // Xóa các activity khác trên stack để tránh back lại chúng
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        btnCourse.setOnClickListener(v -> {
            if (!(this instanceof CourseUserActivity)) {
                Intent intent = new Intent(this, CourseUserActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        btnNotification.setOnClickListener(v -> {
            if (!(this instanceof MenuUserActivity)) {
                Intent intent = new Intent(this, NotificationUserActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        btnMenu.setOnClickListener(v -> {
            // Tương tự cho ProfileActivity
            if (!(this instanceof MenuUserActivity)) {
                Intent intent = new Intent(this, MenuUserActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }
    protected void setupHeader() {

        ImageButton btnTheme = findViewById(R.id.btnThemeSwitch);
        ImageView imgAvatar = findViewById(R.id.iconAvatar);
        ImageView imgDropdown = findViewById(R.id.imgDropdown);

        btnTheme.setOnClickListener(v -> {
            // Xử lý đổi chế độ nền sáng tối
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });
    }
}
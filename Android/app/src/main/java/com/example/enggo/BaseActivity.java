package com.example.enggo;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton; // Hoặc Button, tùy vào view của bạn
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.enggo.R; // Đảm bảo import đúng R của project

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Phương thức này sẽ được gọi trong onCreate của các Activity con
    // sau khi setContentView()
    protected void setupFooter() {
        LinearLayout btnHome = findViewById(R.id.btnHome); // Thay bằng ID thực tế
        LinearLayout btnCourse = findViewById(R.id.btnMyCourse);
        LinearLayout btnSearch = findViewById(R.id.btnSearch);
        LinearLayout btnMenu = findViewById(R.id.btnMenu);

        btnHome.setOnClickListener(v -> {
            // Không thực hiện hành động nếu đang ở HomeActivity
            if (!(this instanceof HomeActivity)) {
                Intent intent = new Intent(this, HomeActivity.class);
                // Xóa các activity khác trên stack để tránh back lại chúng
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        btnCourse.setOnClickListener(v -> {
            if (!(this instanceof CourseActivity)) {
                Intent intent = new Intent(this, CourseActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        btnSearch.setOnClickListener(v -> {
            if (!(this instanceof SearchActivity)) {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        btnMenu.setOnClickListener(v -> {
            // Tương tự cho ProfileActivity
            if (!(this instanceof MenuActivity)) {
                Intent intent = new Intent(this, MenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }
    protected void setupHeader() {
        ImageButton btnNotice = findViewById(R.id.btnNotification);
        ImageButton btnTheme = findViewById(R.id.btnThemeSwitch);
        ImageView imgAvatar = findViewById(R.id.imgAvatar);
        ImageView imgDropdown = findViewById(R.id.imgDropdown);
        btnNotice.setOnClickListener(v -> {
            if (!(this instanceof NotificationActivity)) {
                Intent intent = new Intent(this, NotificationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        btnTheme.setOnClickListener(v -> {
            // Xử lý đổi chế độ nền sáng tối
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });
        imgAvatar.setOnClickListener(v -> {
            if (!(this instanceof ProfileActivity)) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

    }
}

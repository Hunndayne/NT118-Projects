package com.example.enggo.admin;

import com.example.enggo.R;
import com.example.enggo.database.Database;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public abstract class BaseAdminActivity extends AppCompatActivity {

    protected String getTokenFromDb() {
        Database.Dao dao = new Database.Dao(this);
        if (dao.getAll().isEmpty()) {
            return null;
        }
        return dao.getAll().get(0).token;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // ========================================================
    // SETUP HEADER ADMIN
    // ========================================================
    protected void setupAdminHeader() {
        // Ánh xạ các nút từ header_admin.xml
        ImageButton btnTheme = findViewById(R.id.btnThemeSwitch);
        ImageButton btnNotification = findViewById(R.id.btnNotificationAdmin);
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        ImageView imgAvatar = findViewById(R.id.iconAvatar);

        // 1. Nút đổi Theme Sáng/Tối
        // (Tui lấy lại logic từ BaseActivity của học viên, nó y hệt)
        btnTheme.setOnClickListener(v -> {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });

        // 2. Nút thông báo
        btnNotification.setOnClickListener(v -> {
            // Cọu sẽ tạo một AdminNotificationActivity.java sau
            // Intent intent = new Intent(this, AdminNotificationActivity.class);
            // startActivity(intent);
        });

        // 3. Nút Cài đặt
        btnSettings.setOnClickListener(v -> {
            if (!(this instanceof MenuAdminActivity)) {
                Intent intent = new Intent(this, MenuAdminActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // 4. Nút Avatar Admin
        imgAvatar.setOnClickListener(v -> {
            // Cọu sẽ tạo một AdminProfileActivity.java sau
            // Intent intent = new Intent(this, AdminProfileActivity.class);
            // startActivity(intent);
        });
    }

    // ========================================================
    // SETUP FOOTER ADMIN
    // ========================================================
    // Cọu gọi hàm này trong onCreate() CỦA CÁC ACTIVITY CON
    protected void setupAdminFooter() {
        // Ánh xạ các nút từ footer_admin.xml
        LinearLayout btnDashboard = findViewById(R.id.btnDashboard_admin);
        LinearLayout btnCourses = findViewById(R.id.btnCourses_admin);
        LinearLayout btnMenu = findViewById(R.id.btnMenu_admin);
        LinearLayout btnReports = findViewById(R.id.btnReports_admin);

        // Cờ FLAG_ACTIVITY_CLEAR_TOP dùng để xóa các màn hình khác
        // trên stack, giúp cọu điều hướng mượt hơn.

        // 1. Nút Dashboard
        btnDashboard.setOnClickListener(v -> {
            if (!(this instanceof HomeAdminActivity)) {
                Intent intent = new Intent(this, HomeAdminActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // 2. Nút Courses
        btnCourses.setOnClickListener(v -> {
            if (!(this instanceof ManageCoursesAdminActivity)) {
                Intent intent = new Intent(this, ManageCoursesAdminActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // 3. Nút Reports
        btnReports.setOnClickListener(v -> {
            if (!(this instanceof ManageReportsAdminActivity)) {
                Intent intent = new Intent(this, ManageReportsAdminActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // 4. Nút Menu
        btnMenu.setOnClickListener(v -> {
            if (!(this instanceof MenuAdminActivity)) {
                Intent intent = new Intent(this, MenuAdminActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }
}

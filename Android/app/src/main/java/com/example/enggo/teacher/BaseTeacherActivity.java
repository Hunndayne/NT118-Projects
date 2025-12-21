package com.example.enggo.teacher;

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

public abstract class BaseTeacherActivity extends AppCompatActivity {

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
    // SETUP HEADER TEACHER
    // ========================================================
    protected void setupTeacherHeader() {
        ImageButton btnTheme = findViewById(R.id.btnThemeSwitch);
        ImageButton btnNotification = findViewById(R.id.btnNotificationAdmin);
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        ImageView imgAvatar = findViewById(R.id.iconAvatar);

        // 1. Nút đổi Theme
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
            Intent intent = new Intent(this, com.example.enggo.user.NotificationUserActivity.class);
            startActivity(intent);
        });

        // 3. Nút Cài đặt
        btnSettings.setOnClickListener(v -> {
            if (!(this instanceof MenuTeacherActivity)) {
                Intent intent = new Intent(this, MenuTeacherActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // 4. Nút Avatar
        imgAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditUserTeacherActivity.class);
            startActivity(intent);
        });
    }

    // ========================================================
    // SETUP FOOTER TEACHER
    // ========================================================
    protected void setupTeacherFooter() {
        LinearLayout btnDashboard = findViewById(R.id.btnDashboard_teacher);
        LinearLayout btnCourses = findViewById(R.id.btnCourses_teacher);
        LinearLayout btnSchedule = findViewById(R.id.btnSchedule_teacher);
        LinearLayout btnMenu = findViewById(R.id.btnMenu_teacher);

        // 1. Nút Dashboard
        btnDashboard.setOnClickListener(v -> {
            if (!(this instanceof HomeTeacherActivity)) {
                Intent intent = new Intent(this, HomeTeacherActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // 2. Nút Courses
        btnCourses.setOnClickListener(v -> {
            if (!(this instanceof ManageCoursesTeacherActivity)) {
                Intent intent = new Intent(this, ManageCoursesTeacherActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // 3. Nút Schedule
        btnSchedule.setOnClickListener(v -> {
            if (!(this instanceof ScheduleTeacherActivity)) {
                Intent intent = new Intent(this, ScheduleTeacherActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // 4. Nút Menu
        btnMenu.setOnClickListener(v -> {
            if (!(this instanceof MenuTeacherActivity)) {
                Intent intent = new Intent(this, MenuTeacherActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }
}

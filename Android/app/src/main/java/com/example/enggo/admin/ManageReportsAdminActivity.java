package com.example.enggo.admin;
import com.example.enggo.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ManageReportsAdminActivity extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_admin);
        setupAdminHeader();
        setupAdminFooter();

        LinearLayout tvTotalCourses = findViewById(R.id.totalCourses_admin);
        tvTotalCourses.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageCoursesAdminActivity.class);
            startActivity(intent);
        });

        LinearLayout tvStudent = findViewById(R.id.totalStudents_admin);
        tvStudent.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageAccountAdminActivity.class);
            startActivity(intent);
        });
    }
}
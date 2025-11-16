package com.example.enggo.admin;
import com.example.enggo.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class CreateCourseAdminActivity extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_course_admin);

        setupAdminHeader();
        setupAdminFooter();

        Button btnCancel = findViewById(R.id.buttonCancelCourseCreate_admin);
        btnCancel.setOnClickListener(v -> {
            finish();
        });

        Button btnViewandEdit = findViewById(R.id.buttonParticipantsList_admin);
        btnViewandEdit.setOnClickListener(v -> {
            Intent intent = new Intent(CreateCourseAdminActivity.this, CoursesParticipantAdmin.class);
            startActivity(intent);
        });
    }
}
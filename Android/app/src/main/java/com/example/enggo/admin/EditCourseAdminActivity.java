package com.example.enggo.admin;
import com.example.enggo.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class EditCourseAdminActivity extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_course_admin);
        setupAdminHeader();
        setupAdminFooter();

        Button btnView = findViewById(R.id.buttonParticipantsList);

        btnView.setOnClickListener(v -> {
            Intent intent = new Intent(this, CoursesParticipantAdmin.class);
            startActivity(intent);
        });
    }
}
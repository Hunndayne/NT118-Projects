package com.example.enggo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class EditLessonAdminActivity extends BaseAdminActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_lessons_admin);
        setupAdminHeader();
        setupAdminFooter();

        Button btnCancel = findViewById(R.id.buttonCancelLesson);
        btnCancel.setOnClickListener(v -> {
            finish();
        });
    }
}

package com.example.enggo;

import android.os.Bundle;
import android.widget.Button;

public class EditLessonAdminActivity extends BaseAdminActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_lessons_admin);
        setupAdminHeader();
        setupAdminFooter();

        Button btnCancelLesson = findViewById(R.id.buttonCancelLesson);
        btnCancelLesson.setOnClickListener(v -> finish());
    }
}

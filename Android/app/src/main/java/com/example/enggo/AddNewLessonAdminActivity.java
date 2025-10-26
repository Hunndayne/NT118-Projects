package com.example.enggo;

import android.os.Bundle;

public class AddNewLessonAdminActivity extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_lessons_admin);
        setupAdminHeader();
        setupAdminFooter();
    }
}

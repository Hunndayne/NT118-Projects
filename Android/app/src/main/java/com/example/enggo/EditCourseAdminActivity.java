package com.example.enggo;

import android.os.Bundle;

public class EditCourseAdminActivity extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_course_admin);
        setupAdminHeader();
        setupAdminFooter();
    }
}

package com.example.enggo;

import android.os.Bundle;

public class CreateCourseAdminActivity extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_course_admin);

        setupAdminHeader();
        setupAdminFooter();
    }
}

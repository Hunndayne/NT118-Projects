package com.example.enggo;

import android.os.Bundle;

public class ManageCoursesAdminActivity extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_courses_admin);
        setupAdminHeader();
        setupAdminFooter();

    }
}

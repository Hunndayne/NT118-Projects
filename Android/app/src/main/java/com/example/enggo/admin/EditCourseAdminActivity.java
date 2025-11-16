package com.example.enggo.admin;
import com.example.enggo.R;

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
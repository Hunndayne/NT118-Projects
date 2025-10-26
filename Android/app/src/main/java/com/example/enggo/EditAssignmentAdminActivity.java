package com.example.enggo;

import android.os.Bundle;

public class EditAssignmentAdminActivity extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_assignment_admin);
        setupAdminHeader();
        setupAdminFooter();
    }
}

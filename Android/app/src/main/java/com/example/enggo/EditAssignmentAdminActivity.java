package com.example.enggo;

import android.os.Bundle;
import android.widget.Button;

public class EditAssignmentAdminActivity extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_assignment_admin);
        setupAdminHeader();
        setupAdminFooter();

        Button btnCancel = findViewById(R.id.buttonCancelEditAssignment_admin);
        btnCancel.setOnClickListener(v -> {
            finish();
        }); 
    }
}

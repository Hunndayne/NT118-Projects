package com.example.enggo.admin;
import com.example.enggo.R;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class AddAssignmentAdminActivity extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_assignment_teacher);
        setupAdminHeader();
        setupAdminFooter();

        Button btnCancel = findViewById(R.id.buttonCancelAddAssignment_admin);
        Button btnCreate = findViewById(R.id.buttonCreateAssignment_admin);
        Button btnUploadAttachment = findViewById(R.id.buttonAddAttachment_Assignment_admin);
        
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }
        
        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> {
                // TODO: Implement create assignment logic
                Toast.makeText(this, "Create assignment functionality coming soon", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (btnUploadAttachment != null) {
            btnUploadAttachment.setOnClickListener(v -> {
                // TODO: Implement file upload logic
                Toast.makeText(this, "File upload functionality coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
package com.example.enggo.admin;
import com.example.enggo.R;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class EditAssignmentAdminActivity extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_assignment_teacher);
        setupAdminHeader();
        setupAdminFooter();

        Button btnCancel = findViewById(R.id.buttonCancelEditAssignment_admin);
        Button btnSave = findViewById(R.id.buttonSaveEditAssignment_admin);
        Button btnAttachment = findViewById(R.id.buttonAttachment_EditAssignment_admin);
        
        btnCancel.setOnClickListener(v -> finish());
        
        btnSave.setOnClickListener(v -> {
            // TODO: Implement save assignment logic
            Toast.makeText(this, "Save assignment functionality coming soon", Toast.LENGTH_SHORT).show();
            finish();
        });
        
        btnAttachment.setOnClickListener(v -> {
            // TODO: Implement file upload logic
            Toast.makeText(this, "File upload functionality coming soon", Toast.LENGTH_SHORT).show();
        });
    }
}
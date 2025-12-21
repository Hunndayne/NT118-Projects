package com.example.enggo.admin;
import com.example.enggo.R;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class AddNewLessonAdminActivity extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_lessons_teacher);
        setupAdminHeader();
        setupAdminFooter();
        
        Button btnCancel = findViewById(R.id.buttonCancelUpLesson_Admin);
        Button btnUpload = findViewById(R.id.buttonUpLesson_Admin);
        Button btnUploadAttachment = findViewById(R.id.buttonUploadAttachmentAdmin);
        
        btnCancel.setOnClickListener(v -> finish());
        
        btnUpload.setOnClickListener(v -> {
            // TODO: Implement upload lesson logic
            Toast.makeText(this, "Upload lesson functionality coming soon", Toast.LENGTH_SHORT).show();
        });
        
        btnUploadAttachment.setOnClickListener(v -> {
            // TODO: Implement file upload logic
            Toast.makeText(this, "File upload functionality coming soon", Toast.LENGTH_SHORT).show();
        });
    }
}
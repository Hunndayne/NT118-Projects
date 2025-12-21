package com.example.enggo.admin;
import com.example.enggo.R;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class EditLessonAdminActivity extends BaseAdminActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_lessons_teacher);
        setupAdminHeader();
        setupAdminFooter();

        Button btnCancel = findViewById(R.id.buttonCancelLesson);
        Button btnSave = findViewById(R.id.buttonSaveLesson);
        Button btnAttachment = findViewById(R.id.buttonAttachment);
        
        btnCancel.setOnClickListener(v -> finish());
        
        btnSave.setOnClickListener(v -> {
            // TODO: Implement save lesson logic
            Toast.makeText(this, "Save lesson functionality coming soon", Toast.LENGTH_SHORT).show();
            finish();
        });
        
        btnAttachment.setOnClickListener(v -> {
            // TODO: Implement file upload logic
            Toast.makeText(this, "File upload functionality coming soon", Toast.LENGTH_SHORT).show();
        });
    }
}
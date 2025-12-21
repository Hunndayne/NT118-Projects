package com.example.enggo.teacher;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import com.example.enggo.R;

public class EditAssignmentTeacherActivity extends BaseTeacherActivity {
    private EditText etTitle;
    private EditText etContent;
    private EditText etAttachLink;
    private EditText etStartTime;
    private EditText etDueTime;
    private Button btnCancel;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_assignment_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        loadAssignmentData();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etEditAssignmentTitle_admin);
        etContent = findViewById(R.id.etEditAssignmentContent_admin);
        etAttachLink = findViewById(R.id.etAttachLinkContentEditAssignment_admin);
        etStartTime = findViewById(R.id.etStartTimeEditAssignment_admin);
        etDueTime = findViewById(R.id.etDueTimeEditAssignment_admin);
        btnCancel = findViewById(R.id.buttonCancelEditAssignment_admin);
        btnSave = findViewById(R.id.buttonSaveEditAssignment_admin);
    }

    private void loadAssignmentData() {
        // Load assignment data from intent
        if (getIntent() != null) {
            String title = getIntent().getStringExtra("assignment_title");
            String content = getIntent().getStringExtra("assignment_content");
            String attachLink = getIntent().getStringExtra("attach_link");
            String startTime = getIntent().getStringExtra("start_time");
            String dueTime = getIntent().getStringExtra("due_time");
            
            if (title != null && etTitle != null) {
                etTitle.setText(title);
            }
            if (content != null && etContent != null) {
                etContent.setText(content);
            }
            if (attachLink != null && etAttachLink != null) {
                etAttachLink.setText(attachLink);
            }
            if (startTime != null && etStartTime != null) {
                etStartTime.setText(startTime);
            }
            if (dueTime != null && etDueTime != null) {
                etDueTime.setText(dueTime);
            }
        }
    }

    private void setupListeners() {
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                // TODO: Validate and update assignment
                finish();
            });
        }

        if (etStartTime != null) {
            etStartTime.setOnClickListener(v -> {
                // TODO: Show date/time picker
            });
        }

        if (etDueTime != null) {
            etDueTime.setOnClickListener(v -> {
                // TODO: Show date/time picker
            });
        }
    }
}

package com.example.enggo.teacher;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import com.example.enggo.R;

public class AddLessonTeacherActivity extends BaseTeacherActivity {
    private EditText etLessonName;
    private EditText etVideoLink;
    private EditText etPracticeLink;
    private Button btnCancel;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_lessons_teacher);

        setupTeacherHeader();
        setupTeacherFooter();
        initViews();
        setupListeners();
    }

    private void initViews() {
        etLessonName = findViewById(R.id.etLessonTitle_Admin);
        etVideoLink = findViewById(R.id.etAddLessonContent_Admin);
        etPracticeLink = findViewById(R.id.etUpAttachLinkContent_Admim);
        btnCancel = findViewById(R.id.buttonCancelUpLesson_Admin);
        btnCreate = findViewById(R.id.buttonUpLesson_Admin);
    }

    private void setupListeners() {
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> {
                // TODO: Validate and create lesson
                finish();
            });
        }
    }
}

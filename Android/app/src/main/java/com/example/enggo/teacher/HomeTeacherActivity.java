package com.example.enggo.teacher;

import com.example.enggo.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.android.material.card.MaterialCardView;

public class HomeTeacherActivity extends BaseTeacherActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_teacher);

        setupTeacherHeader();
        setupTeacherFooter();

        // Setup onclick handlers
        setupScheduleItems();
        setupGradingCards();
        setupCourseCards();
    }

    private void setupScheduleItems() {
        LinearLayout scheduleItem1 = findViewById(R.id.scheduleItem1_teacher);
        LinearLayout scheduleItem2 = findViewById(R.id.scheduleItem2_teacher);
        
        scheduleItem1.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReportTeacherActivity.class);
            startActivity(intent);
        });
        
        scheduleItem2.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReportTeacherActivity.class);
            startActivity(intent);
        });
    }

    private void setupGradingCards() {
        MaterialCardView cardGradingNew = findViewById(R.id.cardGradingNew_teacher);
        MaterialCardView cardGradingPending = findViewById(R.id.cardGradingPending_teacher);
        
        cardGradingNew.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubmissionListActivity.class);
            startActivity(intent);
        });
        
        cardGradingPending.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubmissionListActivity.class);
            startActivity(intent);
        });
    }

    private void setupCourseCards() {
        LinearLayout courseItem1 = findViewById(R.id.courseItem1_teacher);
        LinearLayout courseItem2 = findViewById(R.id.courseItem2_teacher);
        
        courseItem1.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClassCourseActivity.class);
            startActivity(intent);
        });
        
        courseItem2.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClassCourseActivity.class);
            startActivity(intent);
        });
    }
}

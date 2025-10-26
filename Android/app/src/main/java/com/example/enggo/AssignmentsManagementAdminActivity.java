package com.example.enggo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AssignmentsManagementAdminActivity extends BaseAdminActivity {

    private RecyclerView assignmentsRecyclerView;
    private AssignmentAdminAdapter assignmentAdminAdapter;
    private List<String> assignmentNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_assignments_admin);
        setupAdminHeader();
        setupAdminFooter();

        Button btnAddAssignment = findViewById(R.id.btnAddAssignment);
        btnAddAssignment.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddAssignmentAdminActivity.class);
            startActivity(intent);
        });
        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> {
            finish();
        });

        // === 3. THÊM CODE KHỞI TẠO RECYCLERVIEW ===
        // Ánh xạ RecyclerView
        assignmentsRecyclerView = findViewById(R.id.assignmentsRecyclerView);

        // Tạo dữ liệu tạm
        assignmentNames = new ArrayList<>();
        assignmentNames.add("Assignment 1");
        assignmentNames.add("Midterm Essay");
        assignmentNames.add("Final Project");
        assignmentNames.add("Presentation"); // Thêm bao nhiêu cũng được

        // Khởi tạo Adapter
        assignmentAdminAdapter = new AssignmentAdminAdapter(this, assignmentNames);

        // Gắn Adapter
        assignmentsRecyclerView.setAdapter(assignmentAdminAdapter);
        // === HẾT CODE RECYCLERVIEW ===
    }

}

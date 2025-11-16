package com.example.enggo.admin;
import com.example.enggo.R;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class AddAssignmentAdminActivity extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_assignment_admin);
        setupAdminHeader();
        setupAdminFooter();

//        Button btnCancel = findViewById(R.id.btnCancel);
//        btnCancel.setOnClickListener(v -> {
//            finish();
//        });
    }
}
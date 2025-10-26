package com.example.enggo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class CoursesParticipantAdmin extends BaseAdminActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_view_course_participants_list);

        setupAdminHeader();
        setupAdminFooter();

        Button btnAddParticipant = findViewById(R.id.btnAddUser);
        btnAddParticipant.setOnClickListener(v -> {
            Intent intent = new Intent(this, AvailableUserAdminActivity.class);
            startActivity(intent);
        });
    }
}

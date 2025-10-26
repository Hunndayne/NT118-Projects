package com.example.enggo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class HomeAdminActivity extends BaseAdminActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_admin);

        setupAdminHeader();
        setupAdminFooter();

        TextView btnManageCourses = findViewById(R.id.btnAdmin_ManageCourses);
        TextView btnManageAccount = findViewById(R.id.btnAdmin_ManageAccount);


        btnManageCourses.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, ManageCoursesAdminActivity.class);
            startActivity(intent);
        });

        btnManageAccount.setOnClickListener(v ->{
            Intent intent = new Intent(HomeAdminActivity.this, ManageAccountAdminActivity.class);
            startActivity(intent);
        });
        TextView btnNew = findViewById(R.id.btnAdmin_News);
        btnNew.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        });
    }
}

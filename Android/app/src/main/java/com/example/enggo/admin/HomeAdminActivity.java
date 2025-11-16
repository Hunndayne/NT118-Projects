package com.example.enggo.admin;
import com.example.enggo.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View; // Sửa từ TextView
import android.widget.Button;
import android.widget.TextView;
import com.example.enggo.user.HomeActivity;

public class HomeAdminActivity extends BaseAdminActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_admin);

        setupAdminHeader();
        setupAdminFooter();

        // SỬA LỖI:
        // 1. Đổi kiểu dữ liệu từ TextView -> View (hoặc LinearLayout)
        // 2. Trỏ tới đúng ID của các Layout
        View btnManageCourses = findViewById(R.id.layoutAdmin_ManageCourses); // ID đúng là layoutAdmin_ManageCourses
        View btnManageAccount = findViewById(R.id.layoutAdmin_ManageAccount); // ID đúng là layoutAdmin_ManageAccount
        View btnNew = findViewById(R.id.btnAdmin_News); // ID này là của LinearLayout, không phải TextView


        btnManageCourses.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, ManageCoursesAdminActivity.class);
            startActivity(intent);
        });

        btnManageAccount.setOnClickListener(v ->{
            Intent intent = new Intent(HomeAdminActivity.this, ManageAccountAdminActivity.class);
            startActivity(intent);
        });

        btnNew.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class); // Giả định HomeActivity là đúng
            startActivity(intent);
        });
    }
}

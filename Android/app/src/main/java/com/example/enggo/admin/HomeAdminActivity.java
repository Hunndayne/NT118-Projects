package com.example.enggo.admin;
import com.example.enggo.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.enggo.user.HomeUserActivity;

public class HomeAdminActivity extends BaseAdminActivity{
    ImageView btnEdit1, btnEdit2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_admin);

        setupAdminHeader();
        setupAdminFooter();

        // Setup News section
        LinearLayout layoutNewsList = findViewById(R.id.layoutAdmin_NewsList);
        ImageView imgArrowNews = findViewById(R.id.imgArrowAdmin_News);
        imgArrowNews.setOnClickListener(v -> toggleSection(layoutNewsList, imgArrowNews));

        // Setup Manage Courses section
        LinearLayout layoutManageCourses = findViewById(R.id.layoutAdmin_ManageCourses);
        LinearLayout layoutManageCoursesList = findViewById(R.id.layoutAdmin_ManageCoursesList);
        ImageView imgArrowManageCourses = findViewById(R.id.imgArrowAdmin_ManageCourses);
        imgArrowManageCourses.setOnClickListener(v -> toggleSection(layoutManageCoursesList, imgArrowManageCourses));

        // Click on the whole Manage Courses card to navigate
        layoutManageCourses.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, ManageCoursesAdminActivity.class);
            startActivity(intent);
        });

        // Setup Manage Accounts section
        LinearLayout layoutManageAccount = findViewById(R.id.layoutAdmin_ManageAccount);
        LinearLayout layoutManageAccountList = findViewById(R.id.layoutAdmin_ManageAccountList);
        ImageView imgArrowManageAccount = findViewById(R.id.imgArrowAdmin_ManageAccount);
        imgArrowManageAccount.setOnClickListener(v -> toggleSection(layoutManageAccountList, imgArrowManageAccount));

        // Click on the whole Manage Accounts card to navigate
        layoutManageAccount.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, ManageAccountAdminActivity.class);
            startActivity(intent);
        });

        // Initialize the buttons
        btnEdit1 = findViewById(R.id.btnEdit1);
        btnEdit2 = findViewById(R.id.btnEdit2);

        // Set click listeners for the buttons
        btnEdit1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeAdminActivity.this, EditUserAdminActivity.class);
                startActivity(intent);
            }
        });
        btnEdit2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeAdminActivity.this, EditUserAdminActivity.class);
                startActivity(intent);
            }
        });

        // Initially show all sections
        layoutNewsList.setVisibility(View.VISIBLE);
        imgArrowNews.setRotation(180f);

        layoutManageCoursesList.setVisibility(View.VISIBLE);
        imgArrowManageCourses.setRotation(180f);

        layoutManageAccountList.setVisibility(View.VISIBLE);
        imgArrowManageAccount.setRotation(180f);
    }

    private void toggleSection(View content, ImageView arrow) {
        boolean isVisible = content.getVisibility() == View.VISIBLE;
        content.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        arrow.animate().rotation(isVisible ? 0f : 180f).setDuration(300).start();
    }
}

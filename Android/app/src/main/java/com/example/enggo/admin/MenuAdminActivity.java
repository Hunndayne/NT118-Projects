package com.example.enggo.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.enggo.R;

public class MenuAdminActivity extends BaseAdminActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_admin);

        setupAdminHeader();
        setupAdminFooter();

        ListView adminMenuListView = findViewById(R.id.adminMenuListView);
        String[] accountOptions = getResources().getStringArray(R.array.user_account_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.list_item_account,
                R.id.tv_item_account,
                accountOptions
        );
        adminMenuListView.setAdapter(adapter);

        adminMenuListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            Toast.makeText(MenuAdminActivity.this, selectedItem, Toast.LENGTH_SHORT).show();
            if (position == 0) {
                Intent intent = new Intent(MenuAdminActivity.this, ManageAccountAdminActivity.class);
                startActivity(intent);
            }
        });

        ImageView imAdminAvatar = findViewById(R.id.imAdminAvatar);
        imAdminAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(MenuAdminActivity.this, ManageAccountAdminActivity.class);
            startActivity(intent);
        });

        LinearLayout adminInfoLayout = findViewById(R.id.adminInfoLayout);
        adminInfoLayout.setOnClickListener(v -> {
            Intent intent = new Intent(MenuAdminActivity.this, ManageAccountAdminActivity.class);
            startActivity(intent);
        });
    }
}

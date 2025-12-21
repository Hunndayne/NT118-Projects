package com.example.enggo.teacher;

import com.example.enggo.R;
import com.example.enggo.user.EditInformationUserActivity;
import com.example.enggo.user.ProfileUserActivity;
import com.example.enggo.user.ChangePasswordActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MenuTeacherActivity extends BaseTeacherActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_teacher);

        setupTeacherHeader();
        setupTeacherFooter();

        // Setup back button
        TextView tvBack = findViewById(R.id.tvBackTeacher);
        tvBack.setOnClickListener(v -> finish());

        // Setup avatar and info onclick
        ImageView imTeacherAvatar = findViewById(R.id.imTeacherAvatar);
        LinearLayout teacherInfoLayout = findViewById(R.id.teacherInfoLayout);
        
        imTeacherAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditUserTeacherActivity.class);
            startActivity(intent);
        });
        
        teacherInfoLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditUserTeacherActivity.class);
            startActivity(intent);
        });

        // Setup menu list
        setupMenuOptions();
    }

    private void setupMenuOptions() {
        ListView teacherMenuListView = findViewById(R.id.teacherMenuListView);
        String[] accountOptions = getResources().getStringArray(R.array.user_account_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.list_item_account,
                R.id.tv_item_account,
                accountOptions
        );
        teacherMenuListView.setAdapter(adapter);

        teacherMenuListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            
            switch (position) {
                case 0: // Edit Profile
                    startActivity(new Intent(this, EditInformationUserActivity.class));
                    break;
                case 1: // View Profile
                    startActivity(new Intent(this, ProfileUserActivity.class));
                    break;
                case 2: // Change Password
                    startActivity(new Intent(this, ChangePasswordActivity.class));
                    break;
                default:
                    Toast.makeText(MenuTeacherActivity.this, selectedItem, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}

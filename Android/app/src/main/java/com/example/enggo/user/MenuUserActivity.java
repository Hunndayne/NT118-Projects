package com.example.enggo.user;
import com.example.enggo.R;
import com.example.enggo.auth.ChangePasswordActivity;
import com.example.enggo.common.ChangeAvatarActivity;

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

public class MenuUserActivity extends BaseUserActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        setupHeader();
        setupFooter();
        TextView tvUserName = findViewById(R.id.tvUserName);
        if (tvUserName != null) {
            loadStudentName(tvUserName);
        }
        // 1. Tìm ListView trong layout bằng ID của nó
        ListView userAccountListView = findViewById(R.id.userAccountListView);

        // 2. Lấy mảng dữ liệu chuỗi từ file strings.xml
        String[] accountOptions = getResources().getStringArray(R.array.user_account_options);

        // 3. Tạo một ArrayAdapter để kết nối dữ liệu và giao diện
        // Adapter sẽ lấy dữ liệu (accountOptions) và hiển thị nó lên ListView
        // sử dụng layout cho từng mục (list_item_account.xml)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.list_item_account, // Layout cho từng mục
                R.id.tv_item_account,     // ID của TextView trong layout mục
                accountOptions            // Nguồn dữ liệu
        );

        // 4. Gắn adapter vừa tạo vào ListView
        userAccountListView.setAdapter(adapter);

        // 5. (Tùy chọn) Thêm xử lý sự kiện khi một mục trong danh sách được nhấn
        userAccountListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Lấy nội dung của mục được chọn
                String selectedItem = (String) parent.getItemAtPosition(position);

                // Hiển thị một thông báo nhanh (Toast)
                Toast.makeText(MenuUserActivity.this, "Bạn đã chọn: " + selectedItem, Toast.LENGTH_SHORT).show();

                // Dùng switch-case để xử lý logic cho từng mục
                switch (position) {
                    case 0: // Edit Profile
                        // Viết code để mở màn hình chỉnh sửa profile ở đây
                        Intent intent = new Intent(MenuUserActivity.this, EditInformationUserActivity.class);
                        // Khởi chạy Activity mới
                        startActivity(intent);
                        break;
                    case 1: // Change Password
                        Intent intent1 = new Intent(MenuUserActivity.this, ChangePasswordActivity.class);
                        // Khởi chạy Activity mới
                        startActivity(intent1);
                        break;
                    // ... và các trường hợp khác
                }
            }
        });
        ImageView imAvatar = findViewById(R.id.imAvatar);
        imAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(MenuUserActivity.this, ChangeAvatarActivity.class);
            startActivity(intent);
        });
        LinearLayout userInfoLayout = findViewById(R.id.userInfoLayout);
        userInfoLayout.setOnClickListener(v -> {
            Intent intent = new Intent(MenuUserActivity.this, ProfileUserActivity.class);
            startActivity(intent);
        });
    }
}

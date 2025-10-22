package com.example.enggo; // Thay bằng package của bạn

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class UserAccount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đặt layout cho Activity này là file activity_profile.xml
        setContentView(R.layout.useraccount);

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
                Toast.makeText(UserAccount.this, "Bạn đã chọn: " + selectedItem, Toast.LENGTH_SHORT).show();

                // Dùng switch-case để xử lý logic cho từng mục
                switch (position) {
                    case 0: // Edit Profile
                        // Viết code để mở màn hình chỉnh sửa profile ở đây
                        break;
                    case 1: // Change Password
                        // Viết code để mở màn hình đổi mật khẩu ở đây
                        break;
                    // ... và các trường hợp khác
                }
            }
        });
    }
}

package com.example.enggo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager; // Thêm
import androidx.recyclerview.widget.RecyclerView; // Thêm

import java.util.ArrayList; // Thêm
import java.util.List; // Thêm

// 1. Implement interface của Adapter
public class ManageAccountAdminActivity extends BaseAdminActivity implements UserAdapterAdmin.OnUserActionsListener {

    // 2. Khai báo RecyclerView và Adapter
    private RecyclerView recyclerViewUsers;
    private UserAdapterAdmin userAdapter;
    private List<UserAdmin> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_account_admin);

        setupAdminHeader();
        setupAdminFooter();

        TextView tvBack = findViewById(R.id.tvBack);
        Button btnAddUser = findViewById(R.id.btnAddUser);

        btnAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddUserAdminActivity.class);
            startActivity(intent);
        });

        tvBack.setOnClickListener(v -> {
            finish();
        });

        // 3. Khởi tạo và cài đặt RecyclerView
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        userList = new ArrayList<>();

        // --- Đây là data giả (mock data) để test ---
        // Sau này bạn sẽ thay bằng data lấy từ API/Database
        userList.add(new UserAdmin("Nguyễn Văn A", "van.a@email.com", "Active"));
        userList.add(new UserAdmin("Trần Thị B", "thi.b@email.com", "Locked"));
        userList.add(new UserAdmin("Lê Văn C", "van.c@email.com", "Active"));
        // ------------------------------------------

        // 4. Khởi tạo Adapter và gán vào RecyclerView
        // `this` (cái thứ 3) là để lắng nghe sự kiện click,
        // vì Activity này đã "implements OnUserActionsListener"
        userAdapter = new UserAdapterAdmin(this, userList, this);
        recyclerViewUsers.setAdapter(userAdapter);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
    }

    // 5. Xử lý các sự kiện click (do implement interface)
    // Đây là nơi bạn sẽ code logic cho nút "Edit"
    @Override
    public void onEditClick(UserAdmin user) {
        // Chuyển sang Activity mới và gửi ID của user đi
         Intent intent = new Intent(this, EditUserAdminActivity.class);
         startActivity(intent);
    }

    @Override
    public void onDeleteClick(UserAdmin user) {
        // Thêm logic xóa user và cập nhật lại adapter
    }

    @Override
    public void onLockClick(UserAdmin user) {
        // Thêm logic khóa user và cập nhật lại adapter
    }
}
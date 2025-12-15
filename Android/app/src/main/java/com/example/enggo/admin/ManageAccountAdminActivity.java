package com.example.enggo.admin;
import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager; // Thêm
import androidx.recyclerview.widget.RecyclerView; // Thêm

import java.util.ArrayList; // Thêm
import java.util.List; // Thêm
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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

        userAdapter = new UserAdapterAdmin(this, userList, this);
        recyclerViewUsers.setAdapter(userAdapter);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        loadStudentsFromApi();
    }

    private void loadStudentsFromApi() {
        String token = getTokenFromDb(); // lấy từ SQLite
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getAllStudents(token).enqueue(new Callback<List<UserAdmin>>() {
            @Override
            public void onResponse(Call<List<UserAdmin>> call,
                                   Response<List<UserAdmin>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<UserAdmin>> call, Throwable t) {
                // log lỗi
            }
        });
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
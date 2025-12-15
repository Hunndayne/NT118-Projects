package com.example.enggo.admin;
import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
            startActivityForResult(intent, 1001);
        });

        tvBack.setOnClickListener(v -> {
            finish();
        });

        // 3. Khởi tạo và cài đặt RecyclerView
        setupRecyclerView();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            loadStudentsFromApi();
        }
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
        Intent intent = new Intent(this, EditUserAdminActivity.class);
        intent.putExtra("USER_ID", user.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(UserAdmin user) {

        new AlertDialog.Builder(this)
                .setTitle("Delete user")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Delete", (dialog, which) -> {

                    String token = getTokenFromDb();
                    ApiService apiService = ApiClient.getClient().create(ApiService.class);

                    apiService.deleteUser(token, user.getId())
                            .enqueue(new retrofit2.Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call,
                                                       Response<Void> response) {

                                    if (response.isSuccessful()) {
                                        userList.remove(user);
                                        userAdapter.notifyDataSetChanged();
                                        Toast.makeText(
                                                ManageAccountAdminActivity.this,
                                                "User deleted",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(
                                            ManageAccountAdminActivity.this,
                                            "Delete failed",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }



    @Override
    public void onLockClick(UserAdmin user) {
        // Thêm logic khóa user và cập nhật lại adapter
    }
    public void onResumed() {
        super.onResume();
        loadStudentsFromApi();
    }
}
package com.example.enggo.admin;
import com.example.enggo.R;
import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.admin.CourseAdmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvailableUserAdminActivity extends BaseAdminActivity {
    private static final String TAG = "AvailableUserAdmin";
    private RecyclerView recyclerView;
    private AvailableUsersAdapter adapter;
    private List<CourseParticipant> users;
    private Long courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_course_participants_list);

        setupAdminHeader();
        setupAdminFooter();

        Button btnAddUser = findViewById(R.id.btnAdmin_ParticipantManager);
        btnAddUser.setOnClickListener(v -> {
            Intent intent = new Intent(AvailableUserAdminActivity.this, AddUserAdminActivity.class);
            startActivity(intent);
        });

        courseId = getIntent().getLongExtra(CoursesParticipantAdmin.EXTRA_COURSE_ID, -1);
        if (courseId == -1) {
            Toast.makeText(this, "Missing course id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerAvailableUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        users = new ArrayList<>();
        adapter = new AvailableUsersAdapter(users, this::addParticipant);
        recyclerView.setAdapter(adapter);

        loadEligibleUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEligibleUsers();
    }

    private void loadEligibleUsers() {
        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getEligibleParticipants(token, courseId)
                .enqueue(new Callback<List<CourseParticipant>>() {
                    @Override
                    public void onResponse(Call<List<CourseParticipant>> call,
                                           Response<List<CourseParticipant>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            users.clear();
                            Log.d(TAG, "=== Eligible Users from API ===");
                            Log.d(TAG, "Total users from API: " + response.body().size());
                            for (CourseParticipant participant : response.body()) {
                                String role = participant.getRole();
                                Log.d(TAG, "User: id=" + participant.getId() 
                                    + ", name=" + participant.getFirstName() + " " + participant.getLastName()
                                    + ", role=" + role);
                                if (role == null) {
                                    Log.d(TAG, "  -> SKIPPED (role is null)");
                                    continue;
                                }
                                String normalized = role.trim().toUpperCase();
                                if ("STUDENT".equals(normalized) || "TEACHER".equals(normalized)) {
                                    users.add(participant);
                                    Log.d(TAG, "  -> ADDED to list");
                                } else {
                                    Log.d(TAG, "  -> SKIPPED (role=" + normalized + " not STUDENT/TEACHER)");
                                }
                            }
                            Log.d(TAG, "Final users list size: " + users.size());
                            adapter.notifyDataSetChanged();
                        } else {
                            String errorMsg = "Load available users failed";
                            try {
                                if (response.errorBody() != null) {
                                    errorMsg = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                errorMsg = "Error code: " + response.code();
                            }
                            Log.e(TAG, "Load failed: " + errorMsg);
                            Toast.makeText(
                                    AvailableUserAdminActivity.this,
                                    errorMsg,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<CourseParticipant>> call, Throwable t) {
                        Toast.makeText(
                                AvailableUserAdminActivity.this,
                                "Cannot connect to server",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void addParticipant(CourseParticipant participant) {
        String token = getTokenFromDb();
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        CourseParticipantsRequest request =
                new CourseParticipantsRequest(Collections.singletonList(participant.getId()));

        Log.d(TAG, "=== Adding Participant ===");
        Log.d(TAG, "User ID: " + participant.getId());
        Log.d(TAG, "User Name: " + participant.getFirstName() + " " + participant.getLastName());
        Log.d(TAG, "User Role: " + participant.getRole());
        Log.d(TAG, "Course ID: " + courseId);
        Log.d(TAG, "Request userIds: " + request.userIds);

        apiService.addCourseParticipants(token, courseId, request)
                .enqueue(new Callback<CourseAdmin>() {
                    @Override
                    public void onResponse(Call<CourseAdmin> call, Response<CourseAdmin> response) {
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.isSuccessful()) {
                            Log.d(TAG, "SUCCESS: Participant added");
                            users.remove(participant);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(AvailableUserAdminActivity.this,
                                    "Added: " + participant.getFirstName() + " " + participant.getLastName(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMsg = "Add failed";
                            try {
                                if (response.errorBody() != null) {
                                    errorMsg = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                errorMsg = "Error code: " + response.code();
                            }
                            Log.e(TAG, "FAILED: " + errorMsg);
                            Log.e(TAG, "Response code: " + response.code());
                            Toast.makeText(
                                    AvailableUserAdminActivity.this,
                                    "Add failed [" + participant.getRole() + "]: " + errorMsg,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CourseAdmin> call, Throwable t) {
                        Log.e(TAG, "Network error: " + t.getMessage(), t);
                        Toast.makeText(
                                AvailableUserAdminActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}

package com.example.enggo.api;

import com.example.enggo.admin.UserAdmin;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("checklogin")
    Call<CheckLoginResponse> checkLogin(@Header("X-Auth-Token") String token);
    @POST("auth/logout")
    Call<Void> logout(@Header("X-Auth-Token") String token);
    @GET("admin/students")
    Call<List<UserAdmin>> getAllStudents(
            @Header("X-Auth-Token") String token
    );
    @POST("users")
    Call<UserAdmin> createUser(
            @Header("X-Auth-Token") String token,
            @Body CreateUserRequest request
    );

}
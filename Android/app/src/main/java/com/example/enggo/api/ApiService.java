package com.example.enggo.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @GET("api/checklogin")
    Call<CheckLoginResponse> checkLogin(@Header("X-Auth-Token") String token);

    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

}

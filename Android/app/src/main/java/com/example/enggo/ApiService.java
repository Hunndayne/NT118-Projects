package com.example.enggo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import java.util.List;

// Request body for login
class LoginRequest {
    String username;
    String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

// Response for login
class LoginResponse {
    String token;
    String tokenType;
    String expiresAt;
    boolean admin;
}




class CheckLoginResponse {
    boolean active;
    String expiresAt;
}


public interface ApiService {

    @GET("api/checklogin")
    Call<CheckLoginResponse> checkLogin(@Header("X-Auth-Token") String token);

    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

}

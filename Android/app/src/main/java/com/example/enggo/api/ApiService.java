package com.example.enggo.api;

import com.example.enggo.admin.CourseAdmin;
import com.example.enggo.admin.CourseParticipant;
import com.example.enggo.admin.CourseParticipantsRequest;
import com.example.enggo.admin.CreateCourseRequest;
import com.example.enggo.admin.UpdateCourseRequest;
import com.example.enggo.admin.UserAdmin;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.HTTP;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

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
    @DELETE("admin/users/{id}")
    Call<Void> deleteUser(
            @Header("X-Auth-Token") String token,
            @Path("id") long userId
    );
    @GET("users/{id}")
    Call<UserAdmin> getUserById(
            @Header("X-Auth-Token") String token,
            @Path("id") long userId
    );

    @PUT("users/{id}")
    Call<UserAdmin> updateUser(
            @Header("X-Auth-Token") String token,
            @Path("id") long userId,
            @Body UserUpdateRequest request
    );
    @PUT("admin/users/{id}/lock")
    Call<Void> lockUser(
            @Header("X-Auth-Token") String token,
            @Path("id") long userId
    );

    @PUT("admin/users/{id}/unlock")
    Call<Void> unlockUser(
            @Header("X-Auth-Token") String token,
            @Path("id") long userId
    );
    @GET("courses")
    Call<List<CourseAdmin>> getAllCourses(
            @Header("X-Auth-Token") String token
    );
    @GET("courses/{id}/participants")
    Call<List<CourseParticipant>> getCourseParticipants(
            @Header("X-Auth-Token") String token,
            @Path("id") Long courseId
    );
    @GET("courses/{id}/eligible-participants")
    Call<List<CourseParticipant>> getEligibleParticipants(
            @Header("X-Auth-Token") String token,
            @Path("id") Long courseId
    );
    @POST("/api/courses")
    Call<CourseAdmin> createCourse(
            @Header("X-Auth-Token") String token,
            @Body CreateCourseRequest request
    );
    @GET("/api/admin/courses/{id}")
    Call<CourseAdmin> getCourseById(
            @Header("X-Auth-Token") String token,
            @Path("id") Long id
    );
    @PUT("/api/courses/{id}")
    Call<CourseAdmin> updateCourse(
            @Header("X-Auth-Token") String token,
            @Path("id") Long courseId,
            @Body UpdateCourseRequest request
    );
    @DELETE("courses/{id}")
    Call<Void> deleteCourse(
            @Header("X-Auth-Token") String token,
            @Path("id") Long courseId
    );
    @POST("courses/{id}/participants")
    Call<CourseAdmin> addCourseParticipants(
            @Header("X-Auth-Token") String token,
            @Path("id") Long courseId,
            @Body CourseParticipantsRequest request
    );
    @HTTP(method = "DELETE", path = "courses/{id}/participants", hasBody = true)
    Call<CourseAdmin> removeCourseParticipants(
            @Header("X-Auth-Token") String token,
            @Path("id") Long courseId,
            @Body CourseParticipantsRequest request
    );
}

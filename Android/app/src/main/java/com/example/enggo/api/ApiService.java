package com.example.enggo.api;

import com.example.enggo.admin.CourseAdmin;
import com.example.enggo.admin.CourseParticipant;
import com.example.enggo.admin.CourseParticipantsRequest;
import com.example.enggo.admin.CreateCourseRequest;
import com.example.enggo.admin.UpdateCourseRequest;
import com.example.enggo.admin.UserAdmin;
import com.example.enggo.teacher.AssignmentCreateRequest;
import com.example.enggo.teacher.AssignmentResponse;
import com.example.enggo.teacher.AssignmentUpdateRequest;
import com.example.enggo.teacher.ClassResponse;
import com.example.enggo.teacher.LessonCreateRequest;
import com.example.enggo.teacher.LessonResponse;
import com.example.enggo.teacher.LessonResourceRequest;
import com.example.enggo.teacher.LessonUpdateRequest;
import com.example.enggo.teacher.SubmissionStatusResponse;

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
    @GET("users")
    Call<UserAdmin> getCurrentUser(
            @Header("X-Auth-Token") String token
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
    @GET("classes")
    Call<List<ClassResponse>> getClasses(
            @Header("X-Auth-Token") String token
    );
    @GET("classes/{classId}/lessons")
    Call<List<LessonResponse>> getLessons(
            @Header("X-Auth-Token") String token,
            @Path("classId") Long classId
    );
    @GET("classes/{classId}/assignments")
    Call<List<AssignmentResponse>> getAssignments(
            @Header("X-Auth-Token") String token,
            @Path("classId") Long classId
    );
    @GET("classes/{classId}/assignments/{assignmentId}/submissions")
    Call<List<SubmissionStatusResponse>> getSubmissionStatus(
            @Header("X-Auth-Token") String token,
            @Path("classId") Long classId,
            @Path("assignmentId") Long assignmentId
    );
    @POST("classes/{classId}/lessons")
    Call<LessonResponse> createLesson(
            @Header("X-Auth-Token") String token,
            @Path("classId") Long classId,
            @Body LessonCreateRequest request
    );
    @POST("classes/{classId}/assignments")
    Call<AssignmentResponse> createAssignment(
            @Header("X-Auth-Token") String token,
            @Path("classId") Long classId,
            @Body AssignmentCreateRequest request
    );
    @PUT("classes/{classId}/lessons/{lessonId}")
    Call<LessonResponse> updateLesson(
            @Header("X-Auth-Token") String token,
            @Path("classId") Long classId,
            @Path("lessonId") Long lessonId,
            @Body LessonUpdateRequest request
    );
    @PUT("classes/{classId}/assignments/{assignmentId}")
    Call<AssignmentResponse> updateAssignment(
            @Header("X-Auth-Token") String token,
            @Path("classId") Long classId,
            @Path("assignmentId") Long assignmentId,
            @Body AssignmentUpdateRequest request
    );
    @DELETE("classes/{classId}/lessons/{lessonId}")
    Call<Void> deleteLesson(
            @Header("X-Auth-Token") String token,
            @Path("classId") Long classId,
            @Path("lessonId") Long lessonId
    );
    @DELETE("classes/{classId}/assignments/{assignmentId}")
    Call<Void> deleteAssignment(
            @Header("X-Auth-Token") String token,
            @Path("classId") Long classId,
            @Path("assignmentId") Long assignmentId
    );
    @POST("classes/{classId}/lessons/{lessonId}/resources")
    Call<Void> addLessonResource(
            @Header("X-Auth-Token") String token,
            @Path("classId") Long classId,
            @Path("lessonId") Long lessonId,
            @Body LessonResourceRequest request
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

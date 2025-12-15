package com.finalproject.backend.controller;

import com.finalproject.backend.dto.response.UserResponse;
import com.finalproject.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/students")
    public List<UserResponse> getAllStudents(
            @RequestHeader("X-Auth-Token") String token
    ) {
        return userService.getAllStudents(token);
    }
}

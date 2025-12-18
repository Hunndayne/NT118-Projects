package com.finalproject.backend.controller;

import com.finalproject.backend.dto.response.UserResponse;
import com.finalproject.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/students")
    public List<UserResponse> getAllStudents(@RequestHeader("X-Auth-Token") String token) {
        return userService.getAllStudents(token);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable Long id
    ) {
        userService.deleteUser(token, id);
    }
    @PutMapping("/users/{id}/lock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void lockUser(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable Long id
    ) {
        userService.lockUser(token, id);
    }

    // 🔓 UNLOCK USER
    @PutMapping("/users/{id}/unlock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlockUser(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable Long id
    ) {
        userService.unlockUser(token, id);
    }
}

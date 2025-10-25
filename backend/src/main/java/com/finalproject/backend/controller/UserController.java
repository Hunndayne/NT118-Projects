package com.finalproject.backend.controller;

import com.finalproject.backend.dto.request.UserCreationRequest;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/users")
    User createUser(@Valid @RequestBody UserCreationRequest request) {
        return userService.createUser(request);
    }
    @GetMapping("/users/{id}")
    User getUser(@PathVariable("id") String id) {
        return userService.getUser(id);
    }
}

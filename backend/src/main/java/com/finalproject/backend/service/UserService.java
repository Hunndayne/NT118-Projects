package com.finalproject.backend.service;

import com.finalproject.backend.dto.request.UserCreationRequest;
import com.finalproject.backend.entity.User;
import com.finalproject.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userrepository;

    public User createUser(UserCreationRequest request) {
        User user = new User();

        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmailAddress(request.getEmailAddress());
        user.setCity(request.getCity());
        user.setCountry(request.getCountry());
        user.setTimezone(request.getTimezone());
        user.setPhoneNumber(request.getPhoneNumber());

        return userrepository.save(user);
    }

    public User getUser(String id) {
        return userrepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

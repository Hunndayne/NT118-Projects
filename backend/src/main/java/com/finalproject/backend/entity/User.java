package com.finalproject.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String username;
    private String password;
    private String firstname;
    private String lastname;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "email_visibility")
    private String emailVisibility;

    private String city;
    private String country;
    private String timezone;

    @Column(length = 1000) // nếu mô tả có thể dài
    private String description;

    private String interest;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String profileImagePath;
}


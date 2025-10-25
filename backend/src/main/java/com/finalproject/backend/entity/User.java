package com.finalproject.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(
        name = "Users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email_address", columnNames = "email_address")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String username;
    private String password;
    private String firstname;
    private String lastname;

    @Column(name = "email_address", nullable = false)
    private String emailAddress;

    @Column(name = "email_visibility")
    private String emailVisibility;

    private String city;
    private String country;
    private String timezone;

    @Column(length = 1000)
    private String description;

    private String interest;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String profileImagePath;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Set<UserToken> tokens;
}


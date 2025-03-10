package com.hufs_cheongwon.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RefreshToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private Users users;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @Column(nullable = false, unique = true)
    private String token;

    public void updateToken(String token) {
        this.token = token;
    }

    public String getRole() {
        if (users != null && admin == null) {
            return "ROLE_USER";
        } else if (users == null && admin != null) {
            return "ROLE_ADMIN";
        } else {
            return null;
        }
    }

    public String getEmail() {
        if (users != null && admin == null) {
            return users.getEmail();
        } else if (users == null && admin != null) {
            return admin.getEmail();
        } else {
            return null;
        }
    }

    public Long getId() {
        if (users != null && admin == null) {
            return users.getId();
        } else if (users == null && admin != null) {
            return admin.getId();
        } else {
            return null;
        }
    }
}

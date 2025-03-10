package com.hufs_cheongwon.repository;

import com.hufs_cheongwon.domain.Admin;
import com.hufs_cheongwon.domain.RefreshToken;
import com.hufs_cheongwon.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUsers(Users user);

    Optional<RefreshToken> findByAdmin(Admin admin);

    Optional<RefreshToken> findByToken(String refreshToken);
}

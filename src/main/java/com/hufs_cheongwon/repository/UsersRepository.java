package com.hufs_cheongwon.repository;

import com.hufs_cheongwon.domain.Users;
import com.hufs_cheongwon.domain.enums.UsersStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Boolean existsByEmail(String email);

    Optional<Users> findByEmail(String email);

    void deleteByEmail(String email);

    // usersStatus == INACTIVE && inactiveAt < 30일 전
    List<Users> findAllByUsersStatusAndInactiveAtBefore(UsersStatus usersStatus, LocalDateTime cutoffDate);
}

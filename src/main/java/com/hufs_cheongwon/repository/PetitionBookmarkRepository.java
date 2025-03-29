package com.hufs_cheongwon.repository;

import com.hufs_cheongwon.domain.Petition;
import com.hufs_cheongwon.domain.PetitionBookmark;
import com.hufs_cheongwon.domain.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PetitionBookmarkRepository extends JpaRepository<PetitionBookmark, Long> {

    Optional<PetitionBookmark> findByUsersAndPetition(Users user, Petition petition);

    Page<PetitionBookmark> findAllByUsersOrderByPetitionCreatedAtDesc(Users users, Pageable pageable);

}

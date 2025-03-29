package com.hufs_cheongwon.common.scheduler;

import com.hufs_cheongwon.common.Constant;
import com.hufs_cheongwon.common.Util;
import com.hufs_cheongwon.domain.Users;
import com.hufs_cheongwon.domain.enums.UsersStatus;
import com.hufs_cheongwon.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UsersStatusScheduler {
    private final UsersRepository usersRepository;

    /**
     * 매일 자정에 탈퇴 30일 경과한 유저 정보 삭제
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void eraseInactiveUserInfo() {
        log.info("Withdrawn user info erase job started at: {}", LocalDateTime.now());

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(Constant.USER_WITHDRAW_DATE);
        List<Users> targetUsers = usersRepository.findAllByUsersStatusAndInactiveAtBefore(
                UsersStatus.INACTIVE, cutoffDate);

        for (Users user : targetUsers) {
            user.eraseUserInfo();
            user.changeUserStatus(UsersStatus.DELETED);
            log.info("탈퇴 30일 경과 유저 정보 삭제: email(before)={}", Util.maskEmail(user.getEmail()));
        }

        usersRepository.saveAll(targetUsers);
    }
}

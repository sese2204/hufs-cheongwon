package com.hufs_cheongwon.common.security;

import com.hufs_cheongwon.common.Constant;
import com.hufs_cheongwon.common.Util;
import com.hufs_cheongwon.common.exception.InvalidStateException;
import com.hufs_cheongwon.common.exception.ResourceNotFoundException;
import com.hufs_cheongwon.domain.Users;
import com.hufs_cheongwon.domain.enums.UsersStatus;
import com.hufs_cheongwon.repository.UsersRepository;
import com.hufs_cheongwon.web.apiResponse.error.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    //로그인된 사용자의 정보를 CustomUserDetails에 담아서 반환하는 역할
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("[Authentication] 사용자 CustomUserDetailsService - 입력 이메일: {}", Util.maskEmail(username));

        Users user = usersRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.warn("[Authentication] 사용자 정보 없음 - 이메일: {}", Util.maskEmail(username));
                    return new ResourceNotFoundException(ErrorStatus.USER_NOT_FOUND);
                });

        // 탈퇴한 사용자라면 예외 던지기
        if (user.getUsersStatus() != UsersStatus.ACTIVE) {
            throw new InvalidStateException(ErrorStatus.WITHDRAWN_USER);
        }
        return CustomUserDetails.from(user);
    }
}

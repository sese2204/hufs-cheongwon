package com.hufs_cheongwon.service;

import com.hufs_cheongwon.common.exception.UserNotFoundException;
import com.hufs_cheongwon.common.security.JwtUtil;
import com.hufs_cheongwon.domain.Admin;
import com.hufs_cheongwon.domain.RefreshToken;
import com.hufs_cheongwon.domain.Users;
import com.hufs_cheongwon.repository.RefreshTokenRepository;
import com.hufs_cheongwon.web.apiResponse.error.ErrorStatus;
import com.hufs_cheongwon.web.dto.LoginResponse;
import com.hufs_cheongwon.web.dto.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.antlr.v4.runtime.Token;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.LockInfo;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public void saveRefreshToken(Users user, String refreshToken) {

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUsers(user)
                .orElse(RefreshToken.builder()
                        .users(user)
                        .admin(null)
                        .token(refreshToken)
                        .build());

        refreshTokenEntity.updateToken(refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);
    }

    public void saveRefreshToken(Admin admin, String refreshToken) {

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByAdmin(admin)
                .orElse(RefreshToken.builder()
                        .users(null)
                        .admin(admin)
                        .token(refreshToken)
                        .build());

        refreshTokenEntity.updateToken(refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);
    }

    public LoginResponse reissueToken(String oldRefreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(oldRefreshToken)
                .orElseThrow(() -> new UserNotFoundException(ErrorStatus.REFRESH_TOKEN_NOT_FOUND));

        String role = refreshTokenEntity.getRole();
        String email = refreshTokenEntity.getEmail();

        // 새로운 access token & refresh token 발급
        String newAccessToken = jwtUtil.createAccessToken(email, role);
        String newRefreshToken = jwtUtil.createRefreshToken(email, role);

        // refresh token 교체 (영속성 컨텍스트에서 관리)
        refreshTokenEntity.updateToken(newRefreshToken);

        TokenDto tokenDto = TokenDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
        return LoginResponse.builder()
                .authId(refreshTokenEntity.getId())
                .email(email)
                .role(role)
                .tokenDto(tokenDto)
                .build();
    }
}

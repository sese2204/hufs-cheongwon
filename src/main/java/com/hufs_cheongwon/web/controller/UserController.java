package com.hufs_cheongwon.web.controller;

import com.hufs_cheongwon.common.Constant;
import com.hufs_cheongwon.common.exception.UserNotFoundException;
import com.hufs_cheongwon.common.security.JwtUtil;
import com.hufs_cheongwon.service.RefreshTokenService;
import com.hufs_cheongwon.service.UsersService;
import com.hufs_cheongwon.web.apiResponse.ApiResponse;
import com.hufs_cheongwon.web.apiResponse.error.ErrorStatus;
import com.hufs_cheongwon.web.apiResponse.success.SuccessStatus;
import com.hufs_cheongwon.web.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UsersService usersService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ApiResponse<SignupResponse> registerUser(@Valid @RequestBody LoginRequest request) throws IOException{
        SignupResponse response = usersService.registerUser(request);
        return ApiResponse.onSuccess(SuccessStatus.SIGN_IN_SUCCESS, response);
    }

    //이메일 인증 코드 전송
    @PostMapping("/code/send")
    public ApiResponse<Map<String, Object>> sendEmail(@Valid @RequestBody EmailSendRequest request) throws IOException {

        Map<String, Object> response = usersService.sendEmailCode(request);

        boolean isSuccess = (boolean) response.get("success");
        if(isSuccess) {
            return ApiResponse.onSuccess(SuccessStatus.EMAIL_SENT, null);
        } else {
            return ApiResponse.onFailure( String.valueOf(response.get("code")), (String) response.get("message"), null);
        }
    }

    //코드 인증
    @PostMapping("/code/certify")
    public ApiResponse<Map<String, Object>> certifyEmail(@Valid @RequestBody EmailCertifyRequest request, HttpServletResponse httpServletResponse) throws IOException {

        Map<String, Object> response = usersService.certifyEmailCode(request);

        boolean isSuccess = (boolean) response.get("success");
        if(isSuccess) {
            return ApiResponse.onSuccess(SuccessStatus.EMAIL_VERIFIED, response);
        } else {
            return ApiResponse.onFailure( String.valueOf(response.get("code")), (String) response.get("message"), null);
        }
    }

    //토큰 재발급
    @PostMapping("/reissue")
    public ApiResponse<LoginResponse> reissueUserToken(
            @CookieValue(name = "refresh_token") String refreshToken, HttpServletResponse response) throws IOException{

        if (refreshToken == null) {
            throw new UserNotFoundException(ErrorStatus.COOKIE_EMPTY);
        }

        LoginResponse reissueResponse = refreshTokenService.reissueToken(refreshToken);

        // Refresh Token을 쿠키에 설정
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", reissueResponse.getTokenDto().getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(Constant.REFRESH_COOKIE_EXPIRATION) // 14일(7 * 24 * 60 * 60)
                .path("/")
                .build();
        response.setHeader("Set-Cookie", refreshTokenCookie.toString());

        return ApiResponse.onSuccess(SuccessStatus.REISSUE_TOKEN_SUCCESS, reissueResponse);
    }

    @GetMapping("/test")
    public ApiResponse<Void> testAuthorization() {
        return ApiResponse.onSuccess(SuccessStatus._OK, null);
    }
 }

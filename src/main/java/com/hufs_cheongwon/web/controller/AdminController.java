package com.hufs_cheongwon.web.controller;

import com.hufs_cheongwon.common.Constant;
import com.hufs_cheongwon.service.RefreshTokenService;
import com.hufs_cheongwon.web.apiResponse.ApiResponse;
import com.hufs_cheongwon.web.apiResponse.success.SuccessStatus;
import com.hufs_cheongwon.web.dto.LoginRequest;
import com.hufs_cheongwon.web.dto.LoginResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RefreshTokenService refreshTokenService;

    //해싱한 비밀번호 값을 받아서 디비에 저장하기 위한 매세드 (나중에 다른 방법으로 바꾸는 게 좋을 것 같습니다)
    @GetMapping("/pwd")
    public ApiResponse<Void> registerUser(){
        String encodedPassword = bCryptPasswordEncoder.encode("user1234");
        System.out.println(encodedPassword);
        return ApiResponse.onSuccess(SuccessStatus._OK, null);
    }

    @GetMapping("/test")
    public ApiResponse<Void> testAuthorization(){
        return ApiResponse.onSuccess(SuccessStatus._OK, null);
    }
}

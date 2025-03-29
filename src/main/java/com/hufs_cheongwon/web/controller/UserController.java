package com.hufs_cheongwon.web.controller;

import com.hufs_cheongwon.common.Constant;
import com.hufs_cheongwon.common.exception.ResourceNotFoundException;
import com.hufs_cheongwon.common.security.CustomUserDetails;
import com.hufs_cheongwon.common.security.JwtUtil;
import com.hufs_cheongwon.service.TokenService;
import com.hufs_cheongwon.service.UsersService;
import com.hufs_cheongwon.web.apiResponse.ApiResponse;
import com.hufs_cheongwon.web.apiResponse.error.ErrorStatus;
import com.hufs_cheongwon.web.apiResponse.success.SuccessStatus;
import com.hufs_cheongwon.web.dto.request.EmailCertifyRequest;
import com.hufs_cheongwon.web.dto.request.EmailSendRequest;
import com.hufs_cheongwon.web.dto.request.LoginRequest;
import com.hufs_cheongwon.web.dto.response.LoginResponse;
import com.hufs_cheongwon.web.dto.response.AuthInfoResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UsersService usersService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    //스웨거 문서화 용 (스웨거에서 /user/login으로 요청을 보내도 컨트롤러로 들어오지 않고 jwtUserLoginFilter가 가로채서 로그인을 진행합니다.)
    @PostMapping("/login")
    public ApiResponse<LoginResponse> loginAdmin(@RequestBody LoginRequest request){
        return ApiResponse.onSuccess(SuccessStatus.USER_LOGIN_SUCCESS, null);
    }

    /**
     * 회원가입
     */
    @PostMapping("/register")
    public ApiResponse<AuthInfoResponse> registerUser(@Valid @RequestBody LoginRequest request,
                                                      @CookieValue(name = "email_token") String emailToken) throws IOException{
        String tokenEmail = jwtUtil.getUsername(emailToken);
        AuthInfoResponse response = usersService.registerUser(request, tokenEmail);
        return ApiResponse.onSuccess(SuccessStatus.SIGN_IN_SUCCESS, response);
    }

    /**
     * 이메일 인증 코드 전송
     */
    @PostMapping("/code/send")
    public ApiResponse<Void> sendEmail(@Valid @RequestBody EmailSendRequest request) throws IOException {

        usersService.sendEmailCode(request);
        return ApiResponse.onSuccess(SuccessStatus.EMAIL_SENT, null);
    }

    /**
     * 이메일 인증 코드 인증
     */
    @PostMapping("/code/certify")
    public ApiResponse<String> certifyEmail(@Valid @RequestBody EmailCertifyRequest request, HttpServletResponse response) throws IOException {

        usersService.certifyEmailCode(request);
        String emailToken = jwtUtil.createEmailToken(request.getEmail());

        ResponseCookie emailTokenCookie = ResponseCookie.from("email_token", emailToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(Constant.EMAIL_COOKIE_EXPIRATION) // 10분
                .path("/")
                .build();
        response.addHeader("Set-Cookie", emailTokenCookie.toString());
        return ApiResponse.onSuccess(SuccessStatus.EMAIL_VERIFIED, emailTokenCookie.toString());
    }

    /**
     * 토큰 재발급
     */
    @PostMapping("/reissue")
    public ApiResponse<LoginResponse> reissueUserToken(
            @CookieValue(name = "refresh_token") String refreshToken, HttpServletResponse response) throws IOException{

        if (refreshToken == null) {
            throw new ResourceNotFoundException(ErrorStatus.COOKIE_EMPTY);
        }

        LoginResponse reissueResponse = tokenService.reissueToken(refreshToken);

        // Refresh Token을 쿠키에 설정
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", reissueResponse.getTokenDto().getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(Constant.REFRESH_COOKIE_EXPIRATION) // 14일(7 * 24 * 60 * 60)
                .path("/")
                .build();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return ApiResponse.onSuccess(SuccessStatus.REISSUE_TOKEN_SUCCESS, reissueResponse);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logoutUser(HttpServletRequest request, @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        //헤더에서 access token 추출
        String accessToken = jwtUtil.resolveAccessToken(request);
        String username = jwtUtil.getUsername(accessToken);

        //access token 블랙리스트 등록 & refresh token 삭제
        tokenService.destroyToken(username, accessToken);

        return ApiResponse.onSuccess(SuccessStatus.USER_LOGOUT_SUCCESS, null);
    }

    /**
     * 회원 탈퇴 (soft-delete)
     */
    @PostMapping("/withdraw")
    public ApiResponse<Void> withdrawUser(HttpServletRequest request) {
        //헤더에서 access token 추출
        String accessToken = jwtUtil.resolveAccessToken(request);
        String username = jwtUtil.getUsername(accessToken);

        usersService.withdrawUser(username, accessToken);

        return ApiResponse.onSuccess(SuccessStatus.USER_SIGN_OUT_SUCCESS, null);
    }

    /**
     * 비밀 번호 변경
     */
    @PostMapping("/pwd/update")
    public ApiResponse<AuthInfoResponse> updatePassword(@Valid @RequestBody LoginRequest request,
                                                        @CookieValue(name = "email_token") String emailToken) throws IOException{
        String tokenEmail = jwtUtil.getUsername(emailToken);
        AuthInfoResponse response = usersService.updatePassword(request, tokenEmail);
        return ApiResponse.onSuccess(SuccessStatus.USER_PASSWORD_UPDATE_SUCCESS, response);
    }

    @GetMapping("/test")
    public ApiResponse<Void> testAuthorization() {
        System.out.println(jwtUtil.createJwt("super_user", "ROLE_USER", 365L*24*60*60*1000));
        System.out.println(jwtUtil.createJwt("super_admin", "ROLE_ADMIN", 365L*24*60*60*1000));
        return ApiResponse.onSuccess(SuccessStatus._OK, null);
    }
 }

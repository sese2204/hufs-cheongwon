package com.hufs_cheongwon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hufs_cheongwon.common.security.JwtUtil;
import com.hufs_cheongwon.domain.Admin;
import com.hufs_cheongwon.domain.RefreshToken;
import com.hufs_cheongwon.domain.Users;
import com.hufs_cheongwon.domain.enums.Status;
import com.hufs_cheongwon.repository.AdminRepository;
import com.hufs_cheongwon.repository.RefreshTokenRepository;
import com.hufs_cheongwon.repository.UsersRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String userToken;
    private String adminToken;
    private String userRefreshToken;
    private String adminRefreshToken;



    private final String userRole = "ROLE_USER";
    private final String adminRole = "ROLE_ADMIN";
    private final String USER_EMAIL = "user@example.com";
    private final String ADMIN_EMAIL = "admin@example.com";
    private final String PASSWORD = "user1234";
    private final String WRONG_PASSWORD = "wrongpassword";

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 계정 저장 (비밀번호 암호화 후 DB에 저장)
        Users user = Users.builder()
                .email(USER_EMAIL)
                .password(passwordEncoder.encode(PASSWORD))  // 비밀번호 암호화
                .name("User Test")
                .studentNumber("20230001")
                .status(Status.ACTIVE)
                .build();
        userRepository.save(user);

        // 관리자 계정 저장
        Admin admin = Admin.builder()
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(PASSWORD))  // 비밀번호 암호화
                .build();
        adminRepository.save(admin);

        // JWT 토큰 생성
        userToken = jwtUtil.createAccessToken(USER_EMAIL, "ROLE_USER");
        adminToken = jwtUtil.createAccessToken(ADMIN_EMAIL, "ROLE_ADMIN");

        // 사용자 리프레시 토큰 저장
        userRefreshToken = jwtUtil.createRefreshToken(USER_EMAIL, userRole);
        RefreshToken userToken = RefreshToken.builder()
                .token(userRefreshToken)
                .users(user)
                .admin(null)
                .build();
        refreshTokenRepository.save(userToken);

        // 관리자 리프레시 토큰 저장
        adminRefreshToken = jwtUtil.createRefreshToken(ADMIN_EMAIL, adminRole);
        RefreshToken adminToken = RefreshToken.builder()
                .token(adminRefreshToken)
                .users(null)
                .admin(admin)
                .build();
        refreshTokenRepository.save(adminToken);
    }

    /**
     * 정상적인 사용자 로그인 테스트 (JWT 발급 확인)
     */
    @Test
    void 사용자_로그인_성공() throws Exception {
        // 요청 데이터
        String requestBody = objectMapper.writeValueAsString(new LoginRequest(USER_EMAIL, PASSWORD));

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())  // HTTP 200 응답
                .andExpect(jsonPath("$.result.tokenDto.accessToken").exists())  // JWT Access Token 존재 여부 확인
                .andExpect(jsonPath("$.result.tokenDto.refreshToken").exists()); // JWT Refresh Token 존재 여부 확인
    }

    /**
     * 존재하지 않는 이메일로 로그인 시 실패
     */
    @Test
    void 존재하지_않는_이메일로_로그인_실패() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new LoginRequest("wrong@example.com", PASSWORD));

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())  // HTTP 401 Unauthorized 응답
                .andExpect(jsonPath("$.message").value("존재하지 않는 유저입니다.")); // `USER_NOT_FOUND` 응답 확인
    }

    /**
     * 잘못된 비밀번호로 로그인 시 실패
     */
    @Test
    void 잘못된_비밀번호로_로그인_실패() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new LoginRequest(USER_EMAIL, WRONG_PASSWORD));

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())  // HTTP 401 Unauthorized 응답
                .andExpect(jsonPath("$.message").value("잘못된 비밀번호입니다.")); // `PASSWORD_WRONG` 응답 확인
    }

    /**
     * 유저는 관리자 로그인 불가
     */
    @Test
    void 일반_유저_관리자_로그인_실패() throws Exception {
        // 유저 계정으로 관리자 로그인 시도
        String requestBody = objectMapper.writeValueAsString(new LoginRequest(USER_EMAIL, PASSWORD));

        mockMvc.perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())  // HTTP 401 Unauthorized 응답
                .andExpect(jsonPath("$.message").value("관리자 계정을 찾을 수 없습니다.")); // `ADMIN_NOT_FOUND` 응답 확인
    }

    /**
     * 관리자는 유저 로그인 불가
     */
    @Test
    void 관리자_유저_로그인_실패() throws Exception {
        // 관리자 계정으로 유저 로그인 시도
        String requestBody = objectMapper.writeValueAsString(new LoginRequest(ADMIN_EMAIL, PASSWORD));

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())  // HTTP 401 Unauthorized 응답
                .andExpect(jsonPath("$.message").value("존재하지 않는 유저입니다.")); // `USER_NOT_FOUND` 응답 확인
    }

    /**
     * 로그인한 유저(`ROLE_USER`)가 관리자 API 접근 시 실패
     */
    @Test
    void 유저가_관리자_API_접근_실패() throws Exception {
        mockMvc.perform(get("/admin/test")  // 관리자 전용 API
                        .header("Authorization", "Bearer " + userToken))  // 일반 유저 토큰 사용
                .andExpect(status().isForbidden());  // HTTP 403 Forbidden 응답 확인
    }

    /**
     * 리프레시 토큰 재발급 성공 (쿠키 기반 테스트)
     */
    @Test
    void 리프레시_토큰_재발급_성공() throws Exception {
        String[] newRefreshToken = new String[1];
        mockMvc.perform(post("/user/reissue")
                        .cookie(new Cookie("refresh_token", userRefreshToken)) // 쿠키에 리프레시 토큰 설정
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.tokenDto.accessToken").exists())  // 새로운 액세스 토큰 확인
                .andExpect(jsonPath("$.result.tokenDto.refreshToken").exists()) // 새로운 리프레시 토큰 확인
                .andExpect(jsonPath("$.result.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.result.role").value(userRole))
                .andDo(result -> {
                    // ✅ 새로 발급된 리프레시 토큰 저장
                    String responseBody = result.getResponse().getContentAsString();
                    ObjectMapper objectMapper = new ObjectMapper();
                    newRefreshToken[0] = objectMapper.readTree(responseBody)
                            .path("result")
                            .path("tokenDto")
                            .path("refreshToken")
                            .asText();
                });

        // 새로운 리프레시 토큰이 DB에 저장되었는지 확인
        Optional<RefreshToken> newToken = refreshTokenRepository.findByUsers(userRepository.findByEmail(USER_EMAIL).orElseThrow());
        assertThat(newToken).isPresent();
    }

    /**
     * 존재하지 않는 리프레시 토큰 요청 시 실패 (쿠키 기반)
     */
    @Test
    void 존재하지_않는_리프레시_토큰_실패() throws Exception {
        String invalidRefreshToken = "invalid-refresh-token";

        mockMvc.perform(post("/user/reissue")
                        .cookie(new Cookie("refresh_token", invalidRefreshToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())  // HTTP 404
                .andExpect(jsonPath("$.message").value("존재하지 않는 리프레쉬 토큰입니다."));
    }

    static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}



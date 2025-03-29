package com.hufs_cheongwon.service;

import com.hufs_cheongwon.common.Util;
import com.hufs_cheongwon.common.exception.DuplicateResourceException;
import com.hufs_cheongwon.common.exception.InvalidStateException;
import com.hufs_cheongwon.common.exception.ResourceNotFoundException;
import com.hufs_cheongwon.domain.Users;
import com.hufs_cheongwon.domain.enums.UsersStatus;
import com.hufs_cheongwon.repository.UsersRepository;
import com.hufs_cheongwon.web.apiResponse.error.ErrorStatus;
import com.hufs_cheongwon.web.dto.request.EmailCertifyRequest;
import com.hufs_cheongwon.web.dto.request.EmailSendRequest;
import com.hufs_cheongwon.web.dto.request.LoginRequest;
import com.hufs_cheongwon.web.dto.response.AuthInfoResponse;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UsersService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenService tokenService;
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final CacheService cacheService;

    /**
     * 회원가입
     */
    public AuthInfoResponse registerUser(LoginRequest request, String tokenEmail) throws IOException{
        String email = request.getEmail();
        String password = request.getPassword();

        log.info("[회원가입 요청] 이메일: {}", Util.maskEmail(email));

        // 인증된 이메일인지 확인
        if (!tokenEmail.equals(email)) {
            log.warn("[회원가입 실패] 이메일 불일치 - tokenEmail={}, requestEmail={}", Util.maskEmail(tokenEmail), Util.maskEmail(email));
            throw new ResourceNotFoundException(ErrorStatus.EMAIL_UNCERTIFIED);
        }
        System.out.println(tokenEmail+email);

        // 존재하는 이메일인지 확인
        Users existingUser = usersRepository.findByEmail(email).orElse(null);
        if (existingUser != null){
            if (existingUser.getUsersStatus() == UsersStatus.INACTIVE) {
                // 기존 비활성 유저: 정보 갱신 + 상태 복구
                existingUser.setEncodedPassword(bCryptPasswordEncoder.encode(password));
                existingUser.changeUserStatus(UsersStatus.ACTIVE);
                log.info("[회원 재가입] 기존 INACTIVE 사용자 상태 복원 - email={}", Util.maskEmail(email));
                return AuthInfoResponse.builder()
                        .userId(existingUser.getId())
                        .role(existingUser.getRole())
                        .email(existingUser.getEmail())
                        .build();
            } else {
                //ACTIVE 중복 에러
                throw new DuplicateResourceException(ErrorStatus.EMAIL_DUPLICATED);
            }
        }

        // 새로운 유저
        Users newUser = Users.builder()
                .email(email)
                .usersStatus(UsersStatus.ACTIVE)
                .build();
        newUser.setEncodedPassword(bCryptPasswordEncoder.encode(password));
        Users user = usersRepository.save(newUser);

        log.info("[회원가입 성공] userId={}, email={}", user.getId(), Util.maskEmail(user.getEmail()));

        return AuthInfoResponse.builder()
                .userId(user.getId())
                .role(user.getRole())
                .email(user.getEmail())
                .build();
    }

    /**
     * 이메일 인증 코드 전송
     */
    @Cacheable("MyCache")
    public void sendEmailCode(EmailSendRequest request) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        String email = request.getEmail();
        log.info("[이메일 인증 요청] 이메일: {}", Util.maskEmail(email));

        if (!email.endsWith("@hufs.ac.kr")) {
            log.warn("[이메일 인증 실패] 학교 이메일 아님: {}", Util.maskEmail(email));
            throw new ResourceNotFoundException(ErrorStatus.EMAIL_NOT_SCHOOL);
        }
        try {
            String code = generateAuthCode();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            // 메일을 받을 수신자 설정
            mimeMessageHelper.setTo(email);
            // 메일 제목 설정
            mimeMessageHelper.setSubject("외대 청원 인증번호 발송");
            // html 본문 설정
            Context context = new Context();
            context.setVariable("content", "인증번호: "+code);
            String htmlContent = templateEngine.process("email-template", context);
            // 메일 본문 설정
            mimeMessageHelper.setText(htmlContent, true);
            // 메일 발송
            javaMailSender.send(mimeMessage);
            // 이메일, 인증번호 저장
            cacheService.saveEmailCode(email, code);
            log.info("[이메일 인증번호 발송 완료] 이메일: {}", Util.maskEmail(email));
        } catch (Exception e) {
            log.error("[이메일 인증 실패] 이메일: {}, 오류: {}", Util.maskEmail(email), e.getMessage());
            throw new ResourceNotFoundException(ErrorStatus.AUTH_CODE_SEND_FAIL);
        }
    }

    /**
     * 인증 코드 인증
     */
    @Cacheable("MyCache")
    public void certifyEmailCode(EmailCertifyRequest request) {
        String email = request.getEmail();
        String code = request.getCode();

        log.info("[이메일 인증번호 검증 요청] 이메일: {}", Util.maskEmail(email));

        String validCode = cacheService.getEmailCode(email);
        if (validCode == null) {
            log.warn("[인증 실패] 인증번호 없음 - 이메일: {}", Util.maskEmail(email));
            throw new ResourceNotFoundException(ErrorStatus.AUTH_CODE_NOT_RECEIVED);
        } else if (!validCode.equals(code)) {
            log.warn("[인증 실패] 인증번호 불일치 - 이메일: {}", Util.maskEmail(email));
            throw new ResourceNotFoundException(ErrorStatus.AUTH_CODE_INVALID);
        }
        // 코드가 일치하면 캐시에서 삭제
        cacheService.evictEmailCode(email);
        log.info("[인증 성공] 이메일: {}", Util.maskEmail(email));
    }

    /**
     * 인증코드 생성
     */
    private String generateAuthCode() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder authCode = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            authCode.append(secureRandom.nextInt(10)); // 0~9 랜덤 숫자 생성
        }

        return authCode.toString();
    }

    /**
     * 비밀번호 변경
     */
    public AuthInfoResponse updatePassword(LoginRequest request, String tokenEmail) {
        String email = request.getEmail();
        String password = request.getPassword();

        // 인증된 이메일인지 확인
        if (!tokenEmail.equals(email)) {
            log.warn("[비밀번호 변경 실패] 이메일 불일치 - tokenEmail={}, requestEmail={}", Util.maskEmail(tokenEmail), Util.maskEmail(email));
            throw new ResourceNotFoundException(ErrorStatus.EMAIL_UNCERTIFIED);
        }

        // 가입된 사용자인지 확인
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[비밀번호 변경 실패] 가입되지 않은 이메일: {}", Util.maskEmail(email));
                    return new ResourceNotFoundException(ErrorStatus.USER_NOT_FOUND);
                });

        // 탈퇴한 사용자인지 확인
        if (user.getUsersStatus() != UsersStatus.ACTIVE) {
            log.warn("[비밀번호 변경 실패] 탈퇴한 사용자: {}", Util.maskEmail(email));
            throw new InvalidStateException(ErrorStatus.WITHDRAWN_USER);
        }

        // 비밀번호 변경
        user.setEncodedPassword((bCryptPasswordEncoder.encode(password)));
        return AuthInfoResponse.builder()
                .userId(user.getId())
                .role(user.getRole())
                .email(user.getEmail())
                .build();
    }

    /**
     * 회원 탈퇴 (soft-delete)
     */
    public void withdrawUser(String username, String token) {
        log.info("[회원 탈퇴 요청] 사용자: {}", Util.maskEmail(username));
        // access token 블랙리스트 등록 & refresh token 삭제
        tokenService.destroyToken(username, token);
        Users user = usersRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorStatus.USER_NOT_FOUND));
        //유저 상태 변경
        user.changeUserStatus(UsersStatus.INACTIVE);
        user.setInactiveAt();
    }

    /**
     * 회원 삭제
     */
    public void deleteUser(String username, String token) {
        log.info("[회원 탈퇴 요청] 사용자: {}", Util.maskEmail(username));
        // access token 블랙리스트 등록 & refresh token 삭제
        tokenService.destroyToken(username, token);
        // 디비에서 user 정보 삭제
        usersRepository.deleteByEmail(username);
        log.info("[회원 탈퇴 완료] 사용자: {}", Util.maskEmail(username));
    }
}

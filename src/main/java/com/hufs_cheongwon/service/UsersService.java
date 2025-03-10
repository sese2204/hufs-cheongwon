package com.hufs_cheongwon.service;

import com.hufs_cheongwon.common.exception.DuplicateResourceException;
import com.hufs_cheongwon.common.exception.UserNotFoundException;
import com.hufs_cheongwon.common.security.JwtUtil;
import com.hufs_cheongwon.domain.RefreshToken;
import com.hufs_cheongwon.domain.Users;
import com.hufs_cheongwon.domain.enums.Role;
import com.hufs_cheongwon.domain.enums.Status;
import com.hufs_cheongwon.repository.UsersRepository;
import com.hufs_cheongwon.web.apiResponse.error.ErrorStatus;
import com.hufs_cheongwon.web.dto.*;
import com.univcert.api.UnivCert;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class UsersService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    @Value("${univCert.key}")
    private String univCertKey;
    private String univName = "한국외국어대학교";

    public SignupResponse registerUser(LoginRequest request) throws IOException{

        String email = request.getEmail();
        String password = request.getPassword();

        //존재하는 이메일인지 확인
        Boolean isExist = usersRepository.existsByEmail(email);
        if (isExist){
            throw new DuplicateResourceException(ErrorStatus.EMAIL_DUPLICATED);
        }

        //인증된 이메일인지 확인
        Map<String, Object> univResponse = UnivCert.status(univCertKey, email);
        boolean isCertifiedEmail = (boolean) univResponse.get("success");
        if (isCertifiedEmail) {
            Users newUser = Users.builder()
                    .email(email)
                    .status(Status.ACTIVE)
                    .build();
            newUser.setEncodedPassword(bCryptPasswordEncoder.encode(password));
            Users user = usersRepository.save(newUser);

            return SignupResponse.builder()
                    .userId(user.getId())
                    .role(user.getRole())
                    .email(user.getEmail())
                    .build();
        } else {
            throw new UserNotFoundException(ErrorStatus.EMAIL_UNCERTIFIED);
        }
    }

    public Map<String, Object> sendEmailCode(EmailSendRequest request) throws IOException {
        String email = request.getEmail();
        return UnivCert.certify(univCertKey, email, univName, true);
    }

    public Map<String, Object> certifyEmailCode(EmailCertifyRequest request) throws IOException {
        String email = request.getEmail();
        Integer code = request.getCode();

        Map<String, Object> response = UnivCert.certifyCode(univCertKey, email, univName, code);

        return response;
    }
}

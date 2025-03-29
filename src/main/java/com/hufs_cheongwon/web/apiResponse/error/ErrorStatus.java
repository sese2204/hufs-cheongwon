package com.hufs_cheongwon.web.apiResponse.error;

import com.hufs_cheongwon.web.apiResponse.StatusCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorStatus {

    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, StatusCode.COMMON.getCode(500), "서버 에러, 관리자에게 문의 바랍니다."),
    _GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, StatusCode.COMMON.getCode(504), "서버 에러, 관리자에게 문의 바랍니다."),

    _BAD_REQUEST(HttpStatus.BAD_REQUEST, StatusCode.COMMON.getCode(400), "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, StatusCode.COMMON.getCode(401), "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, StatusCode.COMMON.getCode(403), "금지된 요청입니다."),
    _NOT_FOUND(HttpStatus.NOT_FOUND, StatusCode.COMMON.getCode(404), "찾을 수 없는 요청입니다."),
    _LOGIN_FAILURE(HttpStatus.UNAUTHORIZED, StatusCode.COMMON.getCode(405), "로그인을 실패했습니다."),

    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4001), "존재하지 않는 유저입니다."),
    EMAIL_NOT_EXIST(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4002), "이메일은 필수입니다."),
    EMAIL_INVALID(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4003), "잘못된 이메일 형식입니다."),
    EMAIL_DUPLICATED(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4004), "중복된 이메일입니다."),
    PASSWORD_NOT_EXIST(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4005), "비밀번호는 필수입니다."),
    PASSWORD_INVALID(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4006), "잘못된 비밀번호 형식입니다."),
    PASSWORD_WRONG(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4007), "잘못된 비밀번호입니다."),
    NICKNAME_INVALID(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4008), "잘못된 닉네임 형식입니다."),
    NICKNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4009), "닉네임은 필수입니다."),
    KEY_NOT_FOUNT(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4010), "존재하지 않는 키 값입니다."),
    AUTH_CODE_INVALID(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4011), "잘못된 인증 코드입니다."),
    TOKEN_INVALID(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4012), "로그인이 필요합니다."),
    TOKEN_EXPIRATION(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4013), "만료된 토큰입니다."),
    EMAIL_UNCERTIFIED(HttpStatus.UNAUTHORIZED, StatusCode.USER.getCode(4014), "인증되지 않은 이메일입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, StatusCode.USER.getCode(4015), "존재하지 않는 리프레쉬 토큰입니다."),
    COOKIE_EMPTY(HttpStatus.BAD_REQUEST, StatusCode.COMMON.getCode(4016), "쿠키가 비어있습니다."),
    BLACK_LIST_TOKEN(HttpStatus.BAD_REQUEST, StatusCode.COMMON.getCode(4017), "블랙리스트에 등록된 토큰입니다."),
    EMAIL_NOT_SCHOOL(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4018), "학교 이메일이 아닙니다."),
    AUTH_CODE_SEND_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, StatusCode.USER.getCode(4019), "인증번호 발송에 실패하였습니다."),
    AUTH_CODE_NOT_RECEIVED(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4020), "인증코드를 받은 적이 없는 사용자입니다."),
    WITHDRAWN_USER(HttpStatus.BAD_REQUEST, StatusCode.USER.getCode(4021), "이미 탈퇴된 계정입니다."),

    PETITION_NOT_FOUND(HttpStatus.NOT_FOUND, StatusCode.PETITION.getCode(4001), "해당 청원이 존재하지 않습니다."),
    PETITION_NOT_ONGOING(HttpStatus.BAD_REQUEST, StatusCode.PETITION.getCode(4002), "진행 중인 청원만 동의할 수 있습니다."),
    ALREADY_AGREED(HttpStatus.CONFLICT, StatusCode.PETITION.getCode(4003), "이미 동의한 청원입니다."),
    SELF_AGREEMENT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, StatusCode.PETITION.getCode(4004), "자신의 청원에는 동의할 수 없습니다."),
    SELF_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, StatusCode.PETITION.getCode(4005), "자신의 청원에는 신고할 수 없습니다."),
    ALREADY_REPORTED(HttpStatus.CONFLICT, StatusCode.PETITION.getCode(4006), "이미 신고한 청원입니다."),
    PETITION_TOO_FREQUENT(HttpStatus.CONFLICT, StatusCode.PETITION.getCode(4007), "청원은 7일에 1회만 등록 가능합니다."),

    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, StatusCode.ANSWER.getCode(4001), "해당 답변이 존재하지 않습니다."),
    ALREADY_ANSWERED(HttpStatus.NOT_FOUND, StatusCode.ANSWER.getCode(4002), "이미 답변된 청원입니다."),

    ADMIN_NOT_FOUND(HttpStatus.BAD_REQUEST, StatusCode.ADMIN.getCode(4001), "관리자 계정을 찾을 수 없습니다."),

    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, StatusCode.BOARD.getCode(4001), "해당 게시글이 존재하지 않습니다."),
    INVALID_BOARD_TYPE(HttpStatus.BAD_REQUEST, StatusCode.BOARD.getCode(4002), "잘못된 게시판 타입 요청입니다."),
    KEY_WRONG(HttpStatus.BAD_REQUEST, StatusCode.BOARD.getCode(4003), "잘못된 key 입니다.")
    ;



    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public ErrorResponse getReason(){
        return ErrorResponse.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }
}

package com.hufs_cheongwon.web.apiResponse.success;

import com.hufs_cheongwon.web.apiResponse.StatusCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SuccessStatus {

    _OK(HttpStatus.OK, StatusCode.COMMON.getCode(200), "요청이 성공적으로 처리되었습니다."),
    _CREATED(HttpStatus.CREATED, StatusCode.COMMON.getCode(201), "요청이 성공적으로 생성되었습니다."),

    SIGN_IN_SUCCESS(HttpStatus.OK, StatusCode.USER.getCode(2011), "성공적으로 회원가입되었습니다."),
    USER_LOGOUT_SUCCESS(HttpStatus.OK, StatusCode.USER.getCode(2001), "성공적으로 로그아웃되었습니다."),
    USER_EDIT_SUCCESS(HttpStatus.OK, StatusCode.USER.getCode(2002), "유저 정보가 성공적으로 변경되었습니다."),
    USER_SIGN_OUT_SUCCESS(HttpStatus.OK, StatusCode.USER.getCode(2003), "성공적으로 탈퇴되었습니다."),
    REISSUE_TOKEN_SUCCESS(HttpStatus.OK, StatusCode.USER.getCode(2003), "토큰이 성공적으로 재발급되었습니다."),
    USER_INFO_RETRIEVED(HttpStatus.OK, StatusCode.USER.getCode(2004), "유저 정보가 조회되었습니다."),
    USER_LOGIN_SUCCESS(HttpStatus.OK, StatusCode.USER.getCode(2005), "성공적으로 로그인되었습니다."),
    USER_PASSWORD_UPDATE_SUCCESS(HttpStatus.OK, StatusCode.USER.getCode(2006), "성공적으로 비밀번호가 변경되었습니다."),

    ADMIN_LOGIN_SUCCESS(HttpStatus.OK, StatusCode.ADMIN.getCode(2011), "[관리자] 성공적으로 로그인되었습니다."),
    ADMIN_LOGOUT_SUCCESS(HttpStatus.OK, StatusCode.ADMIN.getCode(2012), "[관리자] 성공적으로 로그아웃되었습니다."),
    ADMIN_SIGN_OUT_SUCCESS(HttpStatus.OK, StatusCode.ADMIN.getCode(2013), "[관리자] 성공적으로 탈퇴되었습니다."),


    ADD_CATEGORY_SUCCESS(HttpStatus.OK, StatusCode.CATEGORY.getCode(2011), "[관리자] 카테고리가 성공적으로 등록되었습니다."),
    EDIT_CATEGORY_SUCCESS(HttpStatus.OK, StatusCode.CATEGORY.getCode(2001), "[관리자] 카테고리가 성공적으로 수정되었습니다."),
    DELETE_CATEGORY_SUCCESS(HttpStatus.OK, StatusCode.CATEGORY.getCode(2002), "[관리자] 카테고리가 성공적으로 삭제되었습니다."),
    CATEGORIES_RETRIEVED(HttpStatus.OK, StatusCode.CATEGORY.getCode(2003), "카테고리 목록이 조회되었습니다."),

    EMAIL_SENT(HttpStatus.OK, StatusCode.EMAIL.getCode(2003), "이메일이 발송되었습니다."),
    EMAIL_VERIFIED(HttpStatus.OK, StatusCode.EMAIL.getCode(2004), "이메일이 인증되었습니다."),

    PETITION_CREATED(HttpStatus.CREATED, StatusCode.PETITION.getCode(2001), "청원이 성공적으로 등록되었습니다."),
    PETITION_DELETED(HttpStatus.OK, StatusCode.PETITION.getCode(2002), "[관리자] 청원이 성공적으로 삭제되었습니다."),
    PETITION_RETRIEVED(HttpStatus.OK, StatusCode.PETITION.getCode(2003), "청원이 성공적으로 조회되었습니다."),
    PETITIONS_RETRIEVED(HttpStatus.OK, StatusCode.PETITION.getCode(2004), "청원 목록이 성공적으로 조회되었습니다."),
    PETITION_UPDATED(HttpStatus.OK, StatusCode.PETITION.getCode(2005), "청원이 성공적으로 수정되었습니다."),
    PETITION_STATUS_UPDATED(HttpStatus.OK, StatusCode.PETITION.getCode(2006), "청원 상태가 성공적으로 변경되었습니다."),
    PETITION_AGREE_SUCCESS(HttpStatus.OK, StatusCode.PETITION.getCode(2007), "청원 동의가 성공적으로 처리되었습니다."),
    PETITION_AGREE_CANCELED(HttpStatus.OK, StatusCode.PETITION.getCode(2008), "청원 동의가 성공적으로 취소되었습니다."),
    PETITION_REPORTED(HttpStatus.OK, StatusCode.PETITION.getCode(2009), "청원 신고가 성공적으로 접수되었습니다."),
    PETITION_SEARCH_SUCCESS(HttpStatus.OK, StatusCode.PETITION.getCode(2010), "청원 검색이 성공적으로 완료되었습니다."),
    PETITION_ANSWERED(HttpStatus.OK, StatusCode.PETITION.getCode(2011), "청원 답변이 성공적으로 등록되었습니다."),
    PETITION_ANSWER_UPDATED(HttpStatus.OK, StatusCode.PETITION.getCode(2012), "청원 답변이 성공적으로 수정되었습니다."),
    PETITION_ANSWER_DELETED(HttpStatus.OK, StatusCode.PETITION.getCode(2013), "청원 답변이 성공적으로 삭제되었습니다."),
    USER_PETITIONS_RETRIEVED(HttpStatus.OK, StatusCode.PETITION.getCode(2014), "사용자의 청원 목록이 성공적으로 조회되었습니다."),
    USER_AGREED_PETITIONS_RETRIEVED(HttpStatus.OK, StatusCode.PETITION.getCode(2015), "사용자가 동의한 청원 목록이 성공적으로 조회되었습니다."),
    POPULAR_PETITIONS_RETRIEVED(HttpStatus.OK, StatusCode.PETITION.getCode(2016), "인기 청원 목록이 성공적으로 조회되었습니다."),
    RECENT_PETITIONS_RETRIEVED(HttpStatus.OK, StatusCode.PETITION.getCode(2017), "최근 청원 목록이 성공적으로 조회되었습니다."),
    ONGOING_PETITIONS_RETRIEVED(HttpStatus.OK, StatusCode.PETITION.getCode(2018), "진행 중인 청원 목록이 성공적으로 조회되었습니다."),
    EXPIRED_PETITIONS_RETRIEVED(HttpStatus.OK, StatusCode.PETITION.getCode(2019), "만료된 청원 목록이 성공적으로 조회되었습니다."),
    WAITING_PETITIONS_RETRIEVED(HttpStatus.OK, StatusCode.PETITION.getCode(2020), "대기 중인 청원 목록이 성공적으로 조회되었습니다."),
    ANSWERED_PETITIONS_RETRIEVED(HttpStatus.OK, StatusCode.PETITION.getCode(2021), "답변된 청원 목록이 성공적으로 조회되었습니다."),
    PETITION_ANSWER_RETRIEVED(HttpStatus.OK, StatusCode.PETITION.getCode(2022), "청원 답변이 성공적으로 조회되었습니다."),
    PETITION_STATS_RETRIEVED(HttpStatus.OK, StatusCode.PETITION.getCode(2023), "청원 통계가 성공적으로 조회되었습니다."),
    PETITION_AGREEMENTS_RETRIEVED(HttpStatus.OK, StatusCode.PETITION.getCode(2024), "청원 동의 목록이 성공적으로 조회되었습니다."),

    BOARDS_RETRIEVED(HttpStatus.OK, StatusCode.BOARD.getCode(2001), "게시판 목록이 성공적으로 조회되었습니다."),
    BOARD_RETRIEVED(HttpStatus.OK, StatusCode.BOARD.getCode(2002), "게시판이 성공적으로 조회되었습니다."),
    ;


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}


package com.hufs_cheongwon.common;

import java.time.Period;

public class Constant {
    public static final Integer REFRESH_COOKIE_EXPIRATION = 7 * 24 * 60 * 60; //7일
    public static final Integer EMAIL_COOKIE_EXPIRATION = 10 * 60; // 10분

    public static final Period PETITION_ACTIVE_PERIOD = Period.ofDays(30);

    public static final Integer THRESHOLDAGREEMENT = 10;

    public static final Integer EXPIRED_DATE = 1;

    public static final Integer USER_WITHDRAW_DATE = 1;
}

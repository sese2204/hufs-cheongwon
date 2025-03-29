package com.hufs_cheongwon.common;

public class Util {
    /**
     * 이메일 마스킹 유틸: abcde@domain.com → a***@d***
     */
    public static String maskEmail(String email) {

        if (email == null) return "invalid";
        else if (email.equals("unknown")) return "unknown";
        else if (!email.contains("@")) return "invalid";

        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        String visibleUsername = username.length() <= 3
                ? username
                : username.substring(0, 3);

        return visibleUsername + "***@" + domain.charAt(0) + "***";
    }
}

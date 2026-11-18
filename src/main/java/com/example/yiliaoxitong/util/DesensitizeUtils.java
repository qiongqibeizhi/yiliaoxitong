package com.example.yiliaoxitong.util;

/**
 * 患者隐私字段脱敏工具。
 * 身份证保留前 6 位与后 4 位，手机号保留前 3 位与后 4 位，其余以星号替代。
 */
public final class DesensitizeUtils {

    private DesensitizeUtils() {
    }

    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        int keep = phone.length() >= 11 ? 3 : 1;
        return phone.substring(0, keep) + "****" + phone.substring(phone.length() - 4);
    }
}

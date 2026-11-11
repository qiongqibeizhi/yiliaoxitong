package com.example.yiliaoxitong.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * 身份证号工具类：校验 18 位身份证号的格式与校验位（GB 11643-1999）。
 * 医疗系统涉及患者敏感身份信息，格式校验在入库前完成。
 */
public final class IdCardUtils {

    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{17}[0-9Xx]$");

    /** 前 17 位加权因子 */
    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /** 模 11 余数对应的校验字符 */
    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    private IdCardUtils() {
    }

    /**
     * 校验格式与校验位，并验证出生日期是否合法。
     */
    public static boolean isValid(String idCard) {
        if (idCard == null || !ID_CARD_PATTERN.matcher(idCard).matches()) {
            return false;
        }
        String birthday = idCard.substring(6, 14);
        try {
            LocalDate.parse(birthday, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return false;
        }
        if (!birthdayValidDate(birthday)) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idCard.charAt(i) - '0') * WEIGHTS[i];
        }
        return CHECK_CODES[sum % 11] == Character.toUpperCase(idCard.charAt(17));
    }

    private static boolean birthdayValidDate(String birthday) {
        int year = Integer.parseInt(birthday.substring(0, 4));
        int month = Integer.parseInt(birthday.substring(4, 6));
        int day = Integer.parseInt(birthday.substring(6, 8));
        return year >= 1900 && year <= LocalDate.now().getYear() && month >= 1 && month <= 12
                && day >= 1 && day <= LocalDate.of(year, month, 1).lengthOfMonth();
    }

    /**
     * 脱敏：保留前 6 位与后 4 位，中间以 * 代替，用于日志与前端展示。
     */
    public static String mask(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }
}

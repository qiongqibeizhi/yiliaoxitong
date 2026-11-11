package com.example.yiliaoxitong.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdCardUtilsTest {

    /** 按 GB 11643-1999 计算校验位，用于构造合法身份证号 */
    private static String withCheckCode(String first17) {
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] codes = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (first17.charAt(i) - '0') * weights[i];
        }
        return first17 + codes[sum % 11];
    }

    @Test
    void validIdCardShouldPass() {
        String id = withCheckCode("43010219900303123");
        assertTrue(IdCardUtils.isValid(id), "合法身份证号应通过校验：" + id);
    }

    @Test
    void wrongCheckCodeShouldFail() {
        String id = withCheckCode("43010219900303123");
        char wrong = id.charAt(17) == '0' ? '1' : '0';
        assertFalse(IdCardUtils.isValid(id.substring(0, 17) + wrong));
    }

    @Test
    void illegalLengthOrCharacterShouldFail() {
        assertFalse(IdCardUtils.isValid("4301021990030312"));
        assertFalse(IdCardUtils.isValid("43010219900303123A"));
        assertFalse(IdCardUtils.isValid(null));
    }

    @Test
    void invalidBirthdayShouldFail() {
        assertFalse(IdCardUtils.isValid(withCheckCode("43010219901332123")));
    }

    @Test
    void maskShouldHideMiddlePart() {
        String id = withCheckCode("43010219900303123");
        String masked = IdCardUtils.mask(id);
        assertEquals(6 + 8 + 4, masked.length());
        assertTrue(masked.startsWith("430102"));
        assertTrue(masked.endsWith(id.substring(14)));
    }
}

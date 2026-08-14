package org.scoula.common.util;

// 하이픈 유무와 관계없이 항상 010-0000-0000 형태로 정규화한다 (동일 번호가 다른 문자열로 중복 가입되는 것 방지)
public final class PhoneUtils {

    private PhoneUtils() {
    }

    public static boolean isValid(String phone) {
        if (phone == null) {
            return false;
        }
        int len = phone.replaceAll("[^0-9]", "").length();
        return len == 10 || len == 11;
    }

    public static String normalize(String phone) {
        if (phone == null) {
            return null;
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        }
        if (digits.length() == 10) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        }
        return phone;
    }
}

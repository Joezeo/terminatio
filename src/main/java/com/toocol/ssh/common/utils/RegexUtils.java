package com.toocol.ssh.common.utils;

import java.util.regex.Pattern;

/**
 * 正则表达式工具类
 *
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/11 11:18
 */
public class RegexUtils {
    public static final int MATCH_IP = 1;

    private static final Pattern IP_PATTERN = Pattern.compile("^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$");

    public static boolean match(String text, int matchType) {
        switch (matchType) {
            case MATCH_IP:
                return matchIp(text);
            default:
                return false;
        }
    }

    public static boolean matchIp(String text) {
        return match(text, IP_PATTERN);
    }

    public static boolean match(String text, Pattern pattern) {
        return pattern.matcher(text).matches();
    }
}

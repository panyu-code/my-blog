package com.panyu.mybolg.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 密码加密工具类
 */
public class PasswordUtil {

    /**
     * 盐值
     */
    private static final String SALT = "blog";

    /**
     * MD5加密密码
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        // 密码 + 盐值进行MD5加密
        return DigestUtils.md5Hex(password + SALT);
    }

    /**
     * 验证密码
     *
     * @param rawPassword       原始密码
     * @param encryptedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encryptedPassword) {
        if (rawPassword == null || encryptedPassword == null) {
            return false;
        }
        return encryptPassword(rawPassword).equals(encryptedPassword);
    }
}

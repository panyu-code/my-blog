package com.panyu.mybolg.util;

import java.util.Random;

/**
 * 验证码工具类
 */
public class CaptchaUtil {
    
    private static final Random RANDOM = new Random();
    private static final int CAPTCHA_LENGTH = 6;
    private static final String DIGITS = "0123456789";
    
    /**
     * 生成数字验证码
     */
    public static String generateCaptcha() {
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            captcha.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }
        return captcha.toString();
    }
    
    /**
     * 生成验证码Redis key
     */
    public static String getCaptchaRedisKey(String email) {
        // 使用小写邮箱来查询，确保一致性
        return "c:" + Math.abs(email.toLowerCase().trim().hashCode());
    }
}

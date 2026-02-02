package com.panyu.mybolg.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 验证码校验工具类
 */
@Component
public class CaptchaValidator {
    
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    
    private static final int MAX_ERROR_COUNT = 3;
    
    /**
     * 校验图形验证码
     */
    public void validateImageCaptcha(String captchaId, String captcha) {
        if (captchaId == null || captchaId.isEmpty()) {
            throw new RuntimeException("验证码ID不能为空");
        }
        
        String storedCode = redisTemplate.opsForValue().get(captchaId);
        if (storedCode == null || storedCode.isEmpty()) {
            throw new RuntimeException("验证码已过期");
        }
        
        // 检查验证码错误次数
        String errorCountKey = "captcha_error_" + captchaId;
        Integer errorCount = getErrorCount(errorCountKey);
        
        if (errorCount >= MAX_ERROR_COUNT) {
            throw new RuntimeException("验证码输入错误次数过多，请重新获取");
        }
        
        if (!storedCode.equalsIgnoreCase(captcha)) {
            // 增加错误计数
            errorCount++;
            redisTemplate.opsForValue().set(errorCountKey, errorCount.toString(), 30, TimeUnit.SECONDS);
            throw new RuntimeException("验证码错误，还有" + (MAX_ERROR_COUNT - errorCount) + "次尝试机会");
        }
        
        // 验证成功，删除验证码和错误计数
        redisTemplate.delete(captchaId);
        redisTemplate.delete(errorCountKey);
    }
    
    /**
     * 校验邮箱验证码
     */
    public void validateEmailCaptcha(String email, String captcha) {
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("邮箱不能为空");
        }
        
        String redisKey = CaptchaUtil.getCaptchaRedisKey(email);
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        
        if (storedCode == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        
        // 检查验证码错误次数
        String errorCountKey = "email_captcha_error_" + email;
        Integer errorCount = getErrorCount(errorCountKey);
        
        if (errorCount >= MAX_ERROR_COUNT) {
            throw new RuntimeException("验证码输入错误次数过多，请重新获取");
        }
        
        if (!storedCode.equals(captcha)) {
            // 增加错误计数
            errorCount++;
            redisTemplate.opsForValue().set(errorCountKey, errorCount.toString(), 1, TimeUnit.MINUTES);
            throw new RuntimeException("验证码错误，还有" + (MAX_ERROR_COUNT - errorCount) + "次尝试机会");
        }
        
        // 验证成功，删除验证码和错误计数
        redisTemplate.delete(redisKey);
        redisTemplate.delete(errorCountKey);
    }
    
    /**
     * 获取错误次数
     */
    private Integer getErrorCount(String errorCountKey) {
        Integer errorCount = 0;
        Object countObj = redisTemplate.opsForValue().get(errorCountKey);
        if (countObj != null) {
            try {
                errorCount = Integer.parseInt(countObj.toString());
            } catch (NumberFormatException e) {
                errorCount = 0;
            }
        }
        return errorCount;
    }
}

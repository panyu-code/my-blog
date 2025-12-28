package com.panyu.mybolg.vo;

import lombok.Data;

/**
 * 登录请求VO
 */
@Data
public class LoginRequest {
    
    /**
     * 用户名（账号登录时使用）
     */
    private String username;
    
    /**
     * 密码（账号登录时使用）
     */
    private String password;
    
    /**
     * 邮箱（邮箱登录时使用）
     */
    private String email;
    
    /**
     * 邮箱验证码（邮箱登录时使用）
     */
    private String emailCaptcha;
    
    /**
     * 图形验证码ID（账号登录时使用）
     */
    private String captchaId;
    
    /**
     * 图形验证码（账号登录时使用）
     */
    private String captcha;
}

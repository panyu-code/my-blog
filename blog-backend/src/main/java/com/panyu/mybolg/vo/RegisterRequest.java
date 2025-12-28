package com.panyu.mybolg.vo;

import lombok.Data;

/**
 * 注册请求VO
 */
@Data
public class RegisterRequest {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 邮箱验证码
     */
    private String emailCaptcha;
}

package com.panyu.mybolg.vo;

import lombok.Data;

/**
 * 找回密码请求VO
 */
@Data
public class ForgotPasswordRequest {
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 验证码
     */
    private String code;
    
    /**
     * 新密码
     */
    private String newPassword;
}

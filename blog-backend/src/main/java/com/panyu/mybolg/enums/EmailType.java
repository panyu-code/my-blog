package com.panyu.mybolg.enums;

import lombok.Getter;

@Getter
public enum EmailType {
    
    /**
     * 验证码邮件
     */
    CAPTCHA("captcha", "验证码"),
    
    /**
     * 找回密码邮件
     */
    FORGOT_PASSWORD("forgot_password", "找回密码"),
    
    /**
     * 文章审核结果邮件
     */
    ARTICLE_AUDIT("article_audit", "文章审核"),
    
    /**
     * 新文章通知邮件
     */
    ARTICLE_NOTIFY("article_notify", "文章通知");
    
    private final String code;
    private final String description;
    
    EmailType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}

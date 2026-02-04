package com.panyu.mybolg.entity;

import com.panyu.mybolg.enums.EmailType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 收件人邮箱
     */
    private String to;
    
    /**
     * 邮件主题
     */
    private String subject;
    
    /**
     * 邮件内容
     */
    private String text;
    
    /**
     * 邮件类型
     */
    private EmailType type;

}

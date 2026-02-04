package com.panyu.mybolg.service;

import com.panyu.mybolg.entity.EmailMessage;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Resource
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String emailFrom;
    
    /**
     * 发送邮件
     */
    public void sendEmail(EmailMessage emailMessage) {
        try {
            logger.info("开始发送邮件: type={}, to={}, subject={}", 
                    emailMessage.getType(), emailMessage.getTo(), emailMessage.getSubject());
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(emailMessage.getTo());
            message.setSubject(emailMessage.getSubject());
            message.setText(emailMessage.getText());
            
            mailSender.send(message);
            
            logger.info("邮件发送成功: type={}, to={}", 
                    emailMessage.getType(), emailMessage.getTo());
        } catch (Exception e) {
            logger.error("邮件发送失败: type={}, to={}, error={}", 
                    emailMessage.getType(), emailMessage.getTo(), e.getMessage(), e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }
}

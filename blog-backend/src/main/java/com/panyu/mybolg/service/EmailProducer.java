package com.panyu.mybolg.service;

import com.panyu.mybolg.entity.EmailMessage;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailProducer.class);
    
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    
    @Value("${rocketmq.email.topic:email-topic}")
    private String emailTopic;
    
    /**
     * 发送邮件消息到队列
     */
    public void sendEmailMessage(EmailMessage emailMessage) {
        try {
            logger.info("发送邮件消息到RocketMQ: type={}, to={}, subject={}", 
                    emailMessage.getType(), emailMessage.getTo(), emailMessage.getSubject());
            
            rocketMQTemplate.syncSend(emailTopic, emailMessage);
            
            logger.info("邮件消息发送成功: type={}, to={}", 
                    emailMessage.getType(), emailMessage.getTo());
        } catch (Exception e) {
            logger.error("发送邮件消息失败: type={}, to={}, error={}", 
                    emailMessage.getType(), emailMessage.getTo(), e.getMessage(), e);
            throw new RuntimeException("发送邮件消息失败: " + e.getMessage(), e);
        }
    }
}

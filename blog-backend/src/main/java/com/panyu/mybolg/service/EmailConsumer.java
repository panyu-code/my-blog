package com.panyu.mybolg.service;

import com.panyu.mybolg.entity.EmailMessage;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(
    topic = "${rocketmq.email.topic:email-topic}",
    consumerGroup = "${rocketmq.email.consumer-group:email-consumer-group}"
)
public class EmailConsumer implements RocketMQListener<EmailMessage> {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailConsumer.class);
    
    @Resource
    private EmailService emailService;
    
    /**
     * 监听邮件队列并处理邮件发送
     */
    @Override
    public void onMessage(EmailMessage emailMessage) {
        try {
            logger.info("接收到邮件消息: type={}, to={}, subject={}", 
                    emailMessage.getType(), emailMessage.getTo(), emailMessage.getSubject());
            
            // 调用邮件发送服务
            emailService.sendEmail(emailMessage);
            
            logger.info("邮件处理完成: type={}, to={}", 
                    emailMessage.getType(), emailMessage.getTo());
        } catch (Exception e) {
            logger.error("处理邮件消息失败: type={}, to={}, error={}", 
                    emailMessage.getType(), emailMessage.getTo(), e.getMessage(), e);
            // 抛出异常以触发重试机制
            throw new RuntimeException("处理邮件消息失败: " + e.getMessage(), e);
        }
    }
}

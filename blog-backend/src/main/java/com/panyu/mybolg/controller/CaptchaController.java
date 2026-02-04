package com.panyu.mybolg.controller;

import com.panyu.mybolg.common.Result;
import com.panyu.mybolg.entity.EmailMessage;
import com.panyu.mybolg.enums.EmailType;
import com.panyu.mybolg.service.EmailProducer;
import com.panyu.mybolg.util.CaptchaUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.panyu.mybolg.util.ImageCaptchaUtil;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Tag(name = "验证码管理", description = "验证码相关接口")
@RestController
@RequestMapping("/captcha")
public class CaptchaController {
    
    private static final Logger logger = LoggerFactory.getLogger(CaptchaController.class);
    
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    
    @Resource
    private EmailProducer emailProducer;
    
    @Operation(summary = "获取图形验证码", description = "获取账号登陆用的图形验证码")
    @GetMapping("/image")
    public Result<Map<String, String>> getImageCaptcha() throws Exception {
        logger.info("请求图形验证码");
        
        // 生成图形验证码
        ImageCaptchaUtil.CaptchaResult result = ImageCaptchaUtil.generateCaptcha();
        String code = result.code;
        BufferedImage image = result.image;
        
        // 生成唯一的captcha ID用于存储和验证
        String captchaId = "img_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
        
        // 存储到Redis，有效期30秒
        redisTemplate.opsForValue().set(captchaId, code, 30, TimeUnit.SECONDS);
        logger.info("图形验证码已生成, captchaId: {}, code: {}", captchaId, code);
        
        // 将图片转换为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();
        
        // Return captcha info (including captchaId and image)
        Map<String, String> data = new HashMap<>();
        data.put("captchaId", captchaId);
        data.put("image", "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(imageBytes));
        
        return Result.success(data);
    }
    
    @Operation(summary = "测试邮件配置", description = "测试邮件配置是否正常")
    @GetMapping("/test")
    public Result<Map<String, String>> testEmail() {
        logger.info("测试邮件配置");
        
        Map<String, String> result = new HashMap<>();
        result.put("status", "success");
        result.put("emailProducer", emailProducer != null ? "exists" : "null");
        logger.info("邮件配置正常");
        return Result.success(result);
    }
    
    @Operation(summary = "发送邮箱验证码", description = "发送邮箱验证码")
    @PostMapping("/send")
    public Result<Map<String, String>> sendCaptcha(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        
        logger.info("接收到发送验证码请求, 邮箱: {}", email);
        
        if (email == null || email.trim().isEmpty()) {
            logger.warn("邮箱为null或为空");
            return Result.error("邮箱不能为空");
        }
        
        logger.info("发件人邮箱配置检查通过");
        
        // 生成验证码
        String captcha = CaptchaUtil.generateCaptcha();
        String redisKey = CaptchaUtil.getCaptchaRedisKey(email);
        
        logger.info("生成的验证码: {}, Redis Key: {}", captcha, redisKey);
        
        try {
            // 存储到Redis，有效期60秒
            redisTemplate.opsForValue().set(redisKey, captcha, 1, TimeUnit.MINUTES);
            logger.info("验证码存储到Redis成功");
            
            // 发送邮件到消息队列
            logger.info("开始发送邮件消息到队列: {}", email);
            EmailMessage emailMessage = new EmailMessage(
                    email,
                    "博客系统 - 验证码",
                    "您的验证码是：" + captcha + "\n\n有效期：1分钟",
                    EmailType.CAPTCHA
            );
            emailProducer.sendEmailMessage(emailMessage);
            logger.info("邮件消息发送完成");
            
            Map<String, String> result = new HashMap<>();
            result.put("message", "验证码已发送");
            return Result.success(result);
        } catch (Exception e) {
            logger.error("发送验证码失败", e);
            return Result.error("发送验证码失败: " + e.getMessage());
        }
    }
}

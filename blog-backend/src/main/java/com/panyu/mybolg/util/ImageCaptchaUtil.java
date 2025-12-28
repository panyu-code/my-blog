package com.panyu.mybolg.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 图形验证码生成工具
 */
public class ImageCaptchaUtil {
    
    private static final Random RANDOM = new Random();
    private static final int WIDTH = 100;
    private static final int HEIGHT = 40;
    private static final int FONT_SIZE = 30;
    private static final String CHARS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final int CAPTCHA_LENGTH = 4;
    
    /**
     * 生成图形验证码
     */
    public static class CaptchaResult {
        public String code;
        public BufferedImage image;
        
        public CaptchaResult(String code, BufferedImage image) {
            this.code = code;
            this.image = image;
        }
    }
    
    /**
     * 生成图形验证码和图片
     */
    public static CaptchaResult generateCaptcha() {
        // 生成验证码
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            code.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        
        // 创建图片
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置背景为纯白色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 绘制干扰线
        g.setColor(new Color(200, 200, 200));
        for (int i = 0; i < 3; i++) {
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
        
        // 绘制验证码
        g.setFont(new Font("Arial", Font.BOLD, FONT_SIZE));
        for (int i = 0; i < code.length(); i++) {
            int x = 10 + i * 20;
            int y = 30;
            
            // 随机颜色
            g.setColor(new Color(RANDOM.nextInt(100), RANDOM.nextInt(100), RANDOM.nextInt(100)));
            
            // 随机旋转
            ((Graphics2D) g).rotate(Math.toRadians(RANDOM.nextInt(30) - 15), x, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            ((Graphics2D) g).rotate(0);
        }
        
        // 绘制边框
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
        
        g.dispose();
        
        return new CaptchaResult(code.toString(), image);
    }
}

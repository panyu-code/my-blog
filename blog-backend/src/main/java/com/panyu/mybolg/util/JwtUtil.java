package com.panyu.mybolg.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JWT 工具类：生成与解析 JWT Token
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretStr;

    // 默认 7 天，毫秒
    @Value("${jwt.expiration:604800000}")
    private long expiration;

    private SecretKey secretKey;
    
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secretStr.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 根据用户ID生成 JWT
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从 Authorization 请求头中解析出用户ID。
     * 格式应为 "Bearer <token>"。
     *
     * @param authorization Authorization 头的值，可为 null
     * @return 用户ID，解析失败或过期返回 null
     */
    public Long getUserIdFromToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7).trim();
        if (token.isEmpty()) {
            return null;
        }
        
        // 检查token是否在黑名单中
        if (isTokenBlacklisted(token)) {
            return null;
        }
        
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String subject = claims.getSubject();
            return subject != null ? Long.parseLong(subject) : null;
        } catch (ExpiredJwtException e) {
            return null;
        } catch (JwtException | NumberFormatException e) {
            return null;
        }
    }

    /**
     * 校验 token 是否有效（未过期且签名正确）
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        // 检查是否在黑名单中
        if (isTokenBlacklisted(token)) {
            return false;
        }
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
    
    /**
     * 将token加入黑名单（用于登出）
     */
    public void blacklistToken(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        try {
            // 获取token的过期时间
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Date expiration = claims.getExpiration();
            long ttl = expiration.getTime() - System.currentTimeMillis();
            
            // 只有未过期的token才需要加入黑名单
            if (ttl > 0) {
                String blacklistKey = "token_blacklist_" + token;
                redisTemplate.opsForValue().set(blacklistKey, "1", ttl, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // token无效，无需加入黑名单
        }
    }
    
    /**
     * 检查token是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        String blacklistKey = "token_blacklist_" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
    }
}

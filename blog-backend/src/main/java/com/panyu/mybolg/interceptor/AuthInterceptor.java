package com.panyu.mybolg.interceptor;

import com.panyu.mybolg.context.UserContext;
import com.panyu.mybolg.exception.UnauthorizedException;
import com.panyu.mybolg.util.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器，统一处理 token 验证
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Resource
    private JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理预检请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        // 获取 Authorization 头
        String authorization = request.getHeader("Authorization");
        
        // 解析用户ID
        Long userId = jwtUtil.getUserIdFromToken(authorization);
        
        if (userId == null) {
            throw new UnauthorizedException("未授权或token已过期");
        }
        
        // 将用户ID存入 ThreadLocal
        UserContext.setUserId(userId);
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后清除 ThreadLocal，避免内存泄漏
        UserContext.clear();
    }
}

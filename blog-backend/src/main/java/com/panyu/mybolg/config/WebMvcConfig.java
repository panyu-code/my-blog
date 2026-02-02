package com.panyu.mybolg.config;

import com.panyu.mybolg.interceptor.AuthInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置，注册拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Resource
    private AuthInterceptor authInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除不需要鉴权的路径
                .excludePathPatterns(
                        // 用户登录注册相关（公开）
                        "/user/login",
                        "/user/register",
                        "/user/admin-login",
                        "/user/check-username",
                        "/user/check-email",
                        "/user/forgot-password/**",
                        
                        // 验证码相关（公开）
                        "/captcha/**",
                        
                        // 公开的文章列表和详情（只读）
                        "/article/list",
                        "/article/admin/list",
                        "/article/{id}",
                        "/article/search",
                        "/article/*/view",
                        "/article/*/like",
                        
                        // 公开的评论列表（只读）
                        "/comment/list",
                        "/comment/*/list",
                        
                        // 分类和标签（只读接口公开，增删改需要鉴权）
                        "/category/list",
                        "/category/{id}",
                        "/category/*/articles",
                        "/tag/list",
                        "/tag/{id}",
                        "/tag/*/articles",
                        
                        // 设置（只读接口公开）
                        "/settings/list",
                        "/settings/{key}",
                        
                        // 天气接口（公开）
                        "/weather/**",
                        
                        // Swagger相关
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        
                        // 静态资源
                        "/static/**",
                        "/public/**",
                        
                        // 错误页面
                        "/error"
                );
    }
}

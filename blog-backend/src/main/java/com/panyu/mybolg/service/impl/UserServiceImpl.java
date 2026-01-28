package com.panyu.mybolg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panyu.mybolg.entity.User;
import com.panyu.mybolg.mapper.UserMapper;
import com.panyu.mybolg.service.UserService;
import com.panyu.mybolg.util.CaptchaUtil;
import com.panyu.mybolg.util.IpUtil;
import com.panyu.mybolg.util.JwtUtil;
import com.panyu.mybolg.util.PasswordUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private JwtUtil jwtUtil;
    
    @Override
    public Map<String, Object> login(String username, String password, HttpServletRequest request) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User loginUser = getOne(wrapper);
        
        // 验证用户是否存在
        if (loginUser == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (loginUser.getStatus() == 0) {
            throw new RuntimeException("用户被禁用");
        }
        
        // 使用MD5验证密码
        if (!PasswordUtil.matches(password, loginUser.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 更新最近登录时间和IP
        loginUser.setLastLoginTime(LocalDateTime.now());
        loginUser.setLastLoginIp(IpUtil.getClientIp(request));
        updateById(loginUser);
        
        // 生成 JWT
        String token = jwtUtil.generateToken(loginUser.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", loginUser);
        
        return result;
    }
    
    @Override
    public Map<String, Object> loginByEmail(String email, String emailCaptcha, HttpServletRequest request) {
        // 从Redis中获取验证码
        String redisKey = CaptchaUtil.getCaptchaRedisKey(email);
        String savedCaptcha = redisTemplate.opsForValue().get(redisKey);
        
        // 验证验证码
        if (savedCaptcha == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        
        // 检查验证码错误次数
        String errorCountKey = "email_captcha_error_" + email;
        Integer errorCount = 0;
        Object countObj = redisTemplate.opsForValue().get(errorCountKey);
        if (countObj != null) {
            errorCount = Integer.parseInt(countObj.toString());
        }
        
        if (errorCount >= 3) {
            throw new RuntimeException("验证码输入错误次数过多，请重新获取");
        }
        
        if (!savedCaptcha.equals(emailCaptcha)) {
            // 增加错误计数
            errorCount++;
            redisTemplate.opsForValue().set(errorCountKey, errorCount.toString(), 1, TimeUnit.MINUTES);
            throw new RuntimeException("验证码错误，还有" + (3 - errorCount) + "次尝试机会");
        }
        
        // 根据邮箱查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User loginUser = getOne(wrapper);
        
        // 验证用户是否存在
        if (loginUser == null) {
            throw new RuntimeException("用户不存在");
        }

        if (loginUser.getStatus() == 0) {
            throw new RuntimeException("用户被禁用");
        }
        
        // 更新最近登录时间和IP
        loginUser.setLastLoginTime(LocalDateTime.now());
        loginUser.setLastLoginIp(IpUtil.getClientIp(request));
        updateById(loginUser);
        
        // 删除已使用的验证码和错误计数
        redisTemplate.delete(redisKey);
        redisTemplate.delete(errorCountKey);
        
        // 生成 JWT
        String token = jwtUtil.generateToken(loginUser.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", loginUser);
        
        return result;
    }
    
    @Override
    public User register(String username, String password, String email, String emailCaptcha) {
        // 验证邮箱验证码
        String redisKey = CaptchaUtil.getCaptchaRedisKey(email);
        String savedCaptcha = redisTemplate.opsForValue().get(redisKey);
        
        if (savedCaptcha == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        
        // 检查验证码错误次数
        String errorCountKey = "email_captcha_error_" + email;
        Integer errorCount = 0;
        Object countObj = redisTemplate.opsForValue().get(errorCountKey);
        if (countObj != null) {
            errorCount = Integer.parseInt(countObj.toString());
        }
        
        if (errorCount >= 3) {
            throw new RuntimeException("验证码输入错误次数过多，请重新获取");
        }
        
        if (!savedCaptcha.equals(emailCaptcha)) {
            // 增加错误计数
            errorCount++;
            redisTemplate.opsForValue().set(errorCountKey, errorCount.toString(), 1, TimeUnit.MINUTES);
            throw new RuntimeException("验证码错误，还有" + (3 - errorCount) + "次尝试机会");
        }
        
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, username);
        User existUser = getOne(usernameWrapper);
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(User::getEmail, email);
        User existEmail = getOne(emailWrapper);
        if (existEmail != null) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        
        // 密码加密
        String encryptedPassword = PasswordUtil.encryptPassword(password);
        user.setPassword(encryptedPassword);
        
        // 设置默认值
        user.setStatus(1); // 正常状态
        user.setRole(0); // 普通用户
        
        // 保存用户
        save(user);
        
        // 删除已使用的验证码和错误计数
        redisTemplate.delete(redisKey);
        redisTemplate.delete(errorCountKey);
        
        return user;
    }
    
    @Override
    public Page<User> listUsers(Integer pageNum, Integer pageSize, String username, String role) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (username != null && !username.trim().isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        if (role != null && !role.trim().isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        
        return page(page, wrapper);
    }
}

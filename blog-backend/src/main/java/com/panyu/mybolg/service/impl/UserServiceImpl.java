package com.panyu.mybolg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panyu.mybolg.entity.User;
import com.panyu.mybolg.mapper.UserMapper;
import com.panyu.mybolg.service.UserService;
import com.panyu.mybolg.util.IpUtil;
import com.panyu.mybolg.util.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
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
        
        // 使用MD5验证密码
        if (!PasswordUtil.matches(password, loginUser.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 更新最近登录时间和IP
        loginUser.setLastLoginTime(LocalDateTime.now());
        loginUser.setLastLoginIp(IpUtil.getClientIp(request));
        updateById(loginUser);
        
        // 生成token
        String token = "token_" + loginUser.getId() + "_" + System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", loginUser);
        
        return result;
    }
    
    @Override
    public User register(User user) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        User existUser = getOne(wrapper);
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 密码加密
        String encryptedPassword = PasswordUtil.encryptPassword(user.getPassword());
        user.setPassword(encryptedPassword);
        
        // 保存用户
        save(user);
        
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

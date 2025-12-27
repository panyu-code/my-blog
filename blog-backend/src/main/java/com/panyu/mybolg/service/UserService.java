package com.panyu.mybolg.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.panyu.mybolg.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface UserService extends IService<User> {
    
    /**
     * 用户登录
     */
    Map<String, Object> login(String username, String password, HttpServletRequest request);
    
    /**
     * 用户注册
     */
    User register(User user);
    
    /**
     * 分页查询用户列表
     */
    Page<User> listUsers(Integer pageNum, Integer pageSize, String username, String role);
}

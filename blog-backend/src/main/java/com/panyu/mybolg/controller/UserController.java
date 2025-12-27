package com.panyu.mybolg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panyu.mybolg.common.Result;
import com.panyu.mybolg.entity.User;
import com.panyu.mybolg.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Resource
    private UserService userService;
    
    @Operation(summary = "用户登录", description = "用户登录接口")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user, HttpServletRequest request) {
        Map<String, Object> result = userService.login(user.getUsername(), user.getPassword(), request);
        return Result.success(result);
    }
    
    @Operation(summary = "用户注册", description = "新用户注册接口")
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        User registeredUser = userService.register(user);
        return Result.success(registeredUser);
    }
    
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户信息")
    @GetMapping("/info")
    public Result<User> getUserInfo(@Parameter(description = "用户ID") @RequestParam Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }
    
    @Operation(summary = "更新用户信息", description = "更新用户信息")
    @PutMapping("/info")
    public Result<User> updateUserInfo(@RequestBody User user) {
        userService.updateById(user);
        return Result.success(user);
    }
    
    @Operation(summary = "用户列表", description = "分页查询用户列表")
    @GetMapping("/list")
    public Result<Page<User>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "角色") @RequestParam(required = false) String role) {
        Page<User> result = userService.listUsers(pageNum, pageSize, username, role);
        return Result.success(result);
    }
    
    @Operation(summary = "删除用户", description = "根据ID删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }
}

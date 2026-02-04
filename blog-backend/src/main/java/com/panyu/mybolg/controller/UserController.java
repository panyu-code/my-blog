package com.panyu.mybolg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panyu.mybolg.common.Result;
import com.panyu.mybolg.context.UserContext;
import com.panyu.mybolg.entity.EmailMessage;
import com.panyu.mybolg.entity.User;
import com.panyu.mybolg.enums.EmailType;
import com.panyu.mybolg.service.EmailProducer;
import com.panyu.mybolg.service.UserService;
import com.panyu.mybolg.util.CaptchaValidator;
import com.panyu.mybolg.util.JwtUtil;
import com.panyu.mybolg.util.PasswordUtil;
import com.panyu.mybolg.vo.ForgotPasswordRequest;
import com.panyu.mybolg.vo.LoginRequest;
import com.panyu.mybolg.vo.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Resource
    private UserService userService;
    
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    
    @Resource
    private EmailProducer emailProducer;

    @Resource
    private JwtUtil jwtUtil;
    
    @Resource
    private CaptchaValidator captchaValidator;
    
    @Operation(summary = "用户登录", description = "支持账号密码、图形验证码和邮箱验证码的登录接口")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        Map<String, Object> result = performLogin(loginRequest, request, false);
        return Result.success(result);
    }
        
    @Operation(summary = "管理员登录", description = "管理员登录接口，需要检查管理员权限")
    @PostMapping("/admin-login")
    public Result<Map<String, Object>> adminLogin(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        Map<String, Object> result = performLogin(loginRequest, request, true);
        return Result.success(result);
    }
        
    /**
     * 统一的登录逻辑处理
     */
    private Map<String, Object> performLogin(LoginRequest loginRequest, HttpServletRequest request, boolean requireAdmin) {
        Map<String, Object> result;
            
        // 判断是账号密码登陆、图形验证码登陆还是邮箱验证码登陆
        if (loginRequest.getEmail() != null && !loginRequest.getEmail().isEmpty()) {
            // 邮箱验证码登录
            captchaValidator.validateEmailCaptcha(loginRequest.getEmail(), loginRequest.getEmailCaptcha());
            result = userService.loginByEmail(loginRequest.getEmail(), loginRequest.getEmailCaptcha(), request);
        } else if (loginRequest.getCaptchaId() != null && !loginRequest.getCaptchaId().isEmpty()) {
            // 账号带图形验证码登录
            captchaValidator.validateImageCaptcha(loginRequest.getCaptchaId(), loginRequest.getCaptcha());
            result = userService.login(loginRequest.getUsername(), loginRequest.getPassword(), request);
        } else {
            // 纯账号密码登录（不需要图形验证码）
            result = userService.login(loginRequest.getUsername(), loginRequest.getPassword(), request);
        }
            
        // 如果是管理员登录，检查用户角色
        if (requireAdmin) {
            User user = (User) result.get("userInfo");
            if (user.getRole() != 1) {
                throw new RuntimeException("您没有权限访问后台管理系统");
            }
        }
            
        return result;
    }
    
    @Operation(summary = "用户注册", description = "新用户注册接口，需要邮箱验证码")
    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest registerRequest) {
        User registeredUser = userService.register(
            registerRequest.getUsername(),
            registerRequest.getPassword(),
            registerRequest.getEmail(),
            registerRequest.getEmailCaptcha()
        );
        return Result.success(registeredUser);
    }
    
    @Operation(summary = "用户登出", description = "用户登出，将token加入黑名单")
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7).trim();
            jwtUtil.blacklistToken(token);
        }
        return Result.success("登出成功");
    }
    
    @Operation(summary = "检查用户名是否存在", description = "检查用户名是否已被注册")
    @GetMapping("/check-username")
    public Result<Map<String, Boolean>> checkUsername(@Parameter(description = "用户名") @RequestParam String username) {
        User user = userService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        Map<String, Boolean> result = new HashMap<>();
        result.put("exists", user != null);
        return Result.success(result);
    }
    
    @Operation(summary = "检查邮箱是否存在", description = "检查邮箱是否已被注册")
    @GetMapping("/check-email")
    public Result<Map<String, Boolean>> checkEmail(@Parameter(description = "邮箱") @RequestParam String email) {
        User user = userService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
        Map<String, Boolean> result = new HashMap<>();
        result.put("exists", user != null);
        return Result.success(result);
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
        // 从 ThreadLocal 获取当前用户ID
        Long userId = UserContext.getUserId();
        
        // 强制使用当前登录用户的ID，防止越权修改他人信息
        user.setId(userId);

        // 使用 UpdateWrapper 仅更新非敏感字段
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<User> updateWrapper = 
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId);
        
        // 只允许更新昵称、邮箱、头像，禁止更新role、status等敏感字段
        if (user.getNickname() != null) {
            updateWrapper.set(User::getNickname, user.getNickname());
        }
        if (user.getEmail() != null) {
            updateWrapper.set(User::getEmail, user.getEmail());
        }
        if (user.getAvatar() != null) {
            updateWrapper.set(User::getAvatar, user.getAvatar());
        }
        
        userService.update(updateWrapper);
        
        // 返回更新后的用户信息
        User updatedUser = userService.getById(userId);
        return Result.success(updatedUser);
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
    
    @Operation(summary = "删除用户", description = "根据 ID删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }
    
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new RuntimeException("原密码不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("新密码不能为空，且长度不能少于6位");
        }
        
        // 从 ThreadLocal 获取当前用户ID
        Long userId = UserContext.getUserId();
        
        User user = userService.getById(userId);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证原密码
        if (!com.panyu.mybolg.util.PasswordUtil.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        // 新密码不能与旧密码相同
        if (com.panyu.mybolg.util.PasswordUtil.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("新密码不能与旧密码相同");
        }
        
        // 加密新密码
        String encryptedPassword = com.panyu.mybolg.util.PasswordUtil.encryptPassword(newPassword);
        user.setPassword(encryptedPassword);
        userService.updateById(user);
        
        return Result.success("密码修改成功");
    }
    
    @Operation(summary = "发送找回密码验证码", description = "根据邮箱发送找回密码验证码")
    @PostMapping("/forgot-password/send-code")
    public Result<String> sendForgotPasswordCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            return Result.error(400, "邮箱不能为空");
        }
        
        // 检查邮箱是否存在
        User user = userService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
        if (user == null) {
            throw new RuntimeException("该邮箱不存在");
        }
        
        // 使用邮箱嬀验证码的同一套流程
        String captcha = com.panyu.mybolg.util.CaptchaUtil.generateCaptcha();
        String redisKey = com.panyu.mybolg.util.CaptchaUtil.getCaptchaRedisKey(email);
        
        // 存储到 Redis，有效期3分钟
        redisTemplate.opsForValue().set(redisKey, captcha, 3, TimeUnit.MINUTES);
        
        try {
            // 发送邮件到消息队列
            EmailMessage emailMessage = new EmailMessage(
                    email,
                    "MyBlog - 找回密码验证码",
                    "您的找回密码验证码是：" + captcha + "\n\n有效期：3分钟\n\n请不要将此验证码告诉他人。",
                    EmailType.FORGOT_PASSWORD
            );
            emailProducer.sendEmailMessage(emailMessage);
            return Result.success("验证码已发送");
        } catch (Exception e) {
            throw new RuntimeException("发送验证码失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "重置密码", description = "根据邮箱和验证码重置用户密码")
    @PostMapping("/forgot-password/reset")
    public Result<String> resetPassword(@RequestBody ForgotPasswordRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        String newPassword = request.getNewPassword();
        
        if (email == null || email.trim().isEmpty()) {
            return Result.error(400, "邮箱不能为空");
        }
        
        if (code == null || code.trim().isEmpty()) {
            return Result.error(400, "验证码不能为空");
        }
        
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error(400, "密码需为 6-20 个字符");
        }
        
        // 验证验证码
        String redisKey = com.panyu.mybolg.util.CaptchaUtil.getCaptchaRedisKey(email);
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        
        if (storedCode == null) {
            throw new RuntimeException("验证码已过期");
        }
        
        if (!storedCode.equals(code)) {
            throw new RuntimeException("验证码错误");
        }
        
        // 根据邮箱找到用户並更新密码
        User user = userService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setPassword(PasswordUtil.encryptPassword(newPassword));
        userService.updateById(user);
        
        // 删除验证码
        redisTemplate.delete(redisKey);
        
        return Result.success("密码重置成功");
    }
}

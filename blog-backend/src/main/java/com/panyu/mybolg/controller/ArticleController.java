package com.panyu.mybolg.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panyu.mybolg.common.Result;
import com.panyu.mybolg.context.UserContext;
import com.panyu.mybolg.entity.Article;
import com.panyu.mybolg.entity.User;
import com.panyu.mybolg.exception.BusinessException;
import com.panyu.mybolg.service.ArticleService;
import com.panyu.mybolg.service.UserService;
import com.panyu.mybolg.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "文章管理", description = "文章相关接口")
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;
    @Resource
    private UserService userService;

    @Resource
    private JwtUtil jwtUtil;

    @Operation(summary = "文章列表", description = "分页查询文章列表，支持标题、分类、状态筛选（Web端：只显示已审核文章）")
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "文章标题") @RequestParam(required = false) String title,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "状态(0:草稿,1:已发布)") @RequestParam(required = false) Integer status) {
        Page<Article> result = articleService.listWithDetails(pageNum, pageSize, title, categoryId, status);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return Result.success(data);
    }

    @Operation(summary = "管理端文章列表", description = "分页查询文章列表，支持标题、分类、状态筛选（管理端：显示所有审核状态的文章）")
    @GetMapping("/admin/list")
    public Result<Map<String, Object>> adminList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "文章标题") @RequestParam(required = false) String title,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "状态(0:草稿,1:已发布)") @RequestParam(required = false) Integer status) {
        Page<Article> result = articleService.listWithDetailsForAdmin(pageNum, pageSize, title, categoryId, status);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return Result.success(data);
    }

    @Operation(summary = "用户文章列表", description = "分页查询用户自己的文章列表，支持标题、状态筛选")
    @GetMapping("/user/list")
    public Result<Map<String, Object>> userArticles(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "文章标题") @RequestParam(required = false) String title,
            @Parameter(description = "状态(0:草稿,1:已发布)") @RequestParam(required = false) Integer status) {
        // 从 ThreadLocal 获取当前用户ID
        Long userId = UserContext.getUserId();
        
        Page<Article> result = articleService.listUserArticles(userId, pageNum, pageSize, title, status);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return Result.success(data);
    }

    @Operation(summary = "获取文章详情", description = "根据ID获取文章详细信息")
    @GetMapping("/{id}")
    public Result<Article> getById(@Parameter(description = "文章ID") @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        // 判断是不是管理员
        User user = userService.getById(userId);
        if(user == null){
            throw new BusinessException(404, "用户不存在");
        }
        Article article = articleService.getById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }
        
        // 只有已发布且已审核通过的文章才能被普通用户访问
        if ((article.getStatus() != 1 || article.getAuditStatus() != 1) && !user.getRole().equals(1)) {
            throw new BusinessException(404, "文章不存在");
        }
        
        // 填充详细信息
        List<Article> articles = new ArrayList<>();
        articles.add(article);
        articleService.fillArticleDetails(articles);
        
        return Result.success(article);
    }

    @Operation(summary = "搜索文章", description = "根据关键词搜索文章")
    @GetMapping("/search")
    public Result<Map<String, Object>> search(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Article> result = articleService.searchArticles(keyword, pageNum, pageSize);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return Result.success(data);
    }

    @Operation(summary = "新增文章", description = "创建新的文章")
    @PostMapping
    public Result<Article> save(@RequestBody Article article) {
        // 如果是草稿状态，则不需要审核；如果是发布状态，则需要审核
        if (article.getAuditStatus() == null) {
            if (article.getStatus() != null && article.getStatus() == 1) { // 已发布状态
                article.setAuditStatus(0); // 待审核
            } else { // 草稿状态或其他状态
                article.setAuditStatus(1); // 直接设置为已审核
            }
        }
        boolean success = articleService.saveWithTags(article, article.getTags());
        if (!success) {
            throw new BusinessException("保存失败");
        }
        return Result.success(article);
    }

    @Operation(summary = "更新文章", description = "更新文章信息（管理端）")
    @PutMapping
    public Result<Article> update(@RequestBody Article article) {
        // 如果文章状态是草稿，则审核状态应设为已审核（因为草稿不需要审核）
        if (article.getStatus() != null && article.getStatus() == 0) { // 草稿状态
            article.setAuditStatus(1); // 直接设置为已审核
        }
        boolean success = articleService.updateWithTags(article, article.getTags());
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success(article);
    }

    @Operation(summary = "更新自己的文章", description = "更新用户自己的文章信息")
    @PutMapping("/user")
    public Result<Article> updateUserArticle(@RequestBody Article article) {
        // 从 ThreadLocal 获取当前用户ID
        Long userId = UserContext.getUserId();
        
        // 验证文章是否属于当前用户
        Article existingArticle = articleService.getById(article.getId());
        if (existingArticle == null) {
            throw new BusinessException(404, "文章不存在");
        }
        
        if (!existingArticle.getAuthorId().equals(userId)) {
            throw new BusinessException(403, "无权限操作此文章");
        }
        
        // 如果文章状态是草稿，则审核状态应设为已审核（因为草稿不需要审核）
        if (article.getStatus() != null && article.getStatus() == 0) { // 草稿状态
            article.setAuditStatus(1); // 直接设置为已审核
        }
        
        boolean success = articleService.updateWithTags(article, article.getTags());
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success(article);
    }

    @Operation(summary = "删除文章", description = "根据ID删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "文章ID") @PathVariable Long id) {
        boolean success = articleService.deleteWithRelations(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success();
    }

    @Operation(summary = "增加浏览量", description = "文章浏览量+1")
    @PostMapping("/{id}/view")
    public Result<Void> increaseView(@Parameter(description = "文章ID") @PathVariable Long id) {
        boolean success = articleService.increaseViewCount(id);
        if (!success) {
            throw new BusinessException("操作失败");
        }
        return Result.success();
    }

    @Operation(summary = "点赞文章", description = "文章点赞数+1")
    @PostMapping("/{id}/like")
    public Result<Void> like(@Parameter(description = "文章ID") @PathVariable Long id) {
        boolean success = articleService.increaseLikeCount(id);
        if (!success) {
            throw new BusinessException("操作失败");
        }
        return Result.success();
    }

    @Operation(summary = "文章审核列表", description = "获取待审核的文章列表")
    @GetMapping("/audit/list")
    public Result<Map<String, Object>> auditList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        // 获取所有文章（包括待审核的）
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        // 只查询已发布且待审核的文章（status = 1 且 auditStatus = 0）
        wrapper.eq(Article::getStatus, 1)      // 已发布的文章
               .eq(Article::getAuditStatus, 0); // 待审核
        wrapper.orderByDesc(Article::getCreateTime);
            
        Page<Article> result = articleService.page(page, wrapper);
        // 填充详细信息
        articleService.fillArticleDetails(result.getRecords());
            
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return Result.success(data);
    }

    @Operation(summary = "审核文章", description = "审核通过或拒绝文章")
    @PostMapping("/{id}/audit")
    public Result<Void> audit(
            @Parameter(description = "文章ID") @PathVariable Long id,
            @Parameter(description = "审核状态: 1-通过 2-拒绝") @RequestParam Integer auditStatus,
            @Parameter(description = "拒绝理由") @RequestParam(required = false) String auditReason) {
        Article article = articleService.getById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }
        article.setAuditStatus(auditStatus);
        if (auditStatus == 2 && auditReason != null && !auditReason.isEmpty()) {
            article.setAuditReason(auditReason);
        }
        boolean success = articleService.updateById(article);
        if (!success) {
            throw new BusinessException("审核失败");
        }
        // 发送邮件通知作者
        articleService.notifyAuthorAuditResult(article);
        return Result.success();
    }

    @Operation(summary = "重新提交审核", description = "将审核不通过的文章重新提交审核")
    @PostMapping("/{id}/resubmit")
    public Result<Void> resubmit(@Parameter(description = "文章ID") @PathVariable Long id) {
        // 从 ThreadLocal 获取当前用户ID
        Long userId = UserContext.getUserId();
        
        Article article = articleService.getById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }
        
        if (!article.getAuthorId().equals(userId)) {
            throw new BusinessException(403, "无权限操作此文章");
        }
        
        // 只有审核不通过的文章才能重新提交
        if (article.getAuditStatus() != 2) {
            throw new BusinessException(400, "只有审核不通过的文章才能重新提交");
        }
        
        // 重新设置为待审核状态
        article.setAuditStatus(0); // 待审核
        article.setAuditReason(null); // 清空审核理由
        boolean success = articleService.updateById(article);
        if (!success) {
            throw new BusinessException("重新提交失败");
        }
        
        // 发送邮件通知管理员
        articleService.notifyAdminNewArticle(article);
        
        return Result.success();
    }

    @Operation(summary = "获取自己的文章详情", description = "根据ID获取用户自己的文章详细信息")
    @GetMapping("/user/{id}")
    public Result<Article> getUserArticleDetail(@Parameter(description = "文章ID") @PathVariable Long id) {
        // 从 ThreadLocal 获取当前用户ID
        Long userId = UserContext.getUserId();
        
        Article article = articleService.getDetailById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }
        
        // 验证文章是否属于当前用户
        if (!article.getAuthorId().equals(userId)) {
            throw new BusinessException(403, "无权限访问此文章");
        }
        
        return Result.success(article);
    }
}

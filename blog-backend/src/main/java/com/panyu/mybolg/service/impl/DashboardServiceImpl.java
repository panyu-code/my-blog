package com.panyu.mybolg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.panyu.mybolg.entity.Article;
import com.panyu.mybolg.entity.Category;
import com.panyu.mybolg.entity.Comment;
import com.panyu.mybolg.entity.User;
import com.panyu.mybolg.service.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {
    
    @Resource
    private ArticleService articleService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private CommentService commentService;
    
    @Resource
    private CategoryService categoryService;
    
    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计已审核通过的文章数
        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.eq(Article::getStatus, 1)  // 已发布的文章
                     .eq(Article::getAuditStatus, 1); // 已审核通过的文章
        long articleCount = articleService.count(articleWrapper);
        stats.put("articleCount", articleCount);
        
        // 统计用户数
        long userCount = userService.count();
        stats.put("userCount", userCount);
        
        // 统计评论数
        long commentCount = commentService.count();
        stats.put("commentCount", commentCount);
        
        // 统计分类数
        long categoryCount = categoryService.count();
        stats.put("categoryCount", categoryCount);
        
        // 统计已审核通过文章的浏览量
        List<Article> articles = articleService.list(articleWrapper);
        int totalViews = articles.stream().mapToInt(Article::getViewCount).sum();
        stats.put("totalViews", totalViews);
        
        return stats;
    }
    
    @Override
    public List<Article> getRecentArticles() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1)      // 已发布的文章
               .eq(Article::getAuditStatus, 1) // 已审核通过的文章
               .orderByDesc(Article::getCreateTime).last("LIMIT 10");
        List<Article> articles = articleService.list(wrapper);
        
        // 填充详细信息
        articleService.fillArticleDetails(articles);
        
        return articles;
    }
    
    @Override
    public List<Comment> getRecentComments() {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Comment::getCreateTime).last("LIMIT 10");
        List<Comment> comments = commentService.list(wrapper);
        
        // 填充用户信息
        fillUserInfo(comments);
        
        return comments;
    }
    
    @Override
    public Map<String, Object> getVisitTrend() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取最近7天的日期
        List<String> dates = new ArrayList<>();
        List<Integer> views = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(formatter));
            
            // 查询该天的已审核通过文章浏览量总和
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(Article::getCreateTime, startOfDay)
                   .lt(Article::getCreateTime, endOfDay)
                   .eq(Article::getStatus, 1)      // 已发布的文章
                   .eq(Article::getAuditStatus, 1); // 已审核通过的文章
            
            List<Article> articles = articleService.list(wrapper);
            int totalViews = articles.stream().mapToInt(Article::getViewCount).sum();
            views.add(totalViews);
        }
        
        result.put("dates", dates);
        result.put("views", views);
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getCategoryStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 获取所有分类
        List<Category> categories = categoryService.list();
        
        for (Category category : categories) {
            // 统计每个分类的已审核通过的文章数
            LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Article::getCategoryId, category.getId())
                   .eq(Article::getStatus, 1)      // 已发布的文章
                   .eq(Article::getAuditStatus, 1); // 已审核通过的文章
            long count = articleService.count(wrapper);
            
            if (count > 0) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("name", category.getName());
                stat.put("value", count);
                result.add(stat);
            }
        }
        
        return result;
    }
    
    /**
     * 填充评论的用户信息
     */
    private void fillUserInfo(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        
        // 收集所有用户ID
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return;
        }
        
        // 批量查询用户
        List<User> users = userService.listByIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        // 填充用户信息
        comments.forEach(comment -> {
            if (comment.getUserId() != null && comment.getUserId() > 0) {
                comment.setUser(userMap.get(comment.getUserId()));
            }
        });
    }
}

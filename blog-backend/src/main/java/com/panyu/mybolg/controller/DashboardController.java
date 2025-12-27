package com.panyu.mybolg.controller;

import com.panyu.mybolg.common.Result;
import com.panyu.mybolg.entity.Article;
import com.panyu.mybolg.entity.Comment;
import com.panyu.mybolg.service.DashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = dashboardService.getStats();
        return Result.success(stats);
    }

    @GetMapping("/recent-articles")
    public Result<List<Article>> getRecentArticles() {
        List<Article> articles = dashboardService.getRecentArticles();
        return Result.success(articles);
    }

    @GetMapping("/recent-comments")
    public Result<List<Comment>> getRecentComments() {
        List<Comment> comments = dashboardService.getRecentComments();
        return Result.success(comments);
    }

    @GetMapping("/visit-trend")
    public Result<Map<String, Object>> getVisitTrend() {
        Map<String, Object> trend = dashboardService.getVisitTrend();
        return Result.success(trend);
    }

    @GetMapping("/category-stats")
    public Result<List<Map<String, Object>>> getCategoryStats() {
        List<Map<String, Object>> stats = dashboardService.getCategoryStats();
        return Result.success(stats);
    }
}

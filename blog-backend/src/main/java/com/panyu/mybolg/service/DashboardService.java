package com.panyu.mybolg.service;

import com.panyu.mybolg.entity.Article;
import com.panyu.mybolg.entity.Comment;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    
    /**
     * 获取统计数据
     */
    Map<String, Object> getStats();
    
    /**
     * 获取最近文章
     */
    List<Article> getRecentArticles();
    
    /**
     * 获取最近评论
     */
    List<Comment> getRecentComments();
    
    /**
     * 获取访问趋势
     */
    Map<String, Object> getVisitTrend();
    
    /**
     * 获取分类统计
     */
    List<Map<String, Object>> getCategoryStats();
}

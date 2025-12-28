package com.panyu.mybolg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panyu.mybolg.entity.Article;
import com.panyu.mybolg.entity.Category;
import com.panyu.mybolg.mapper.ArticleMapper;
import com.panyu.mybolg.mapper.CategoryMapper;
import com.panyu.mybolg.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    
    @Resource
    private ArticleMapper articleMapper;
    
    @Resource
    private ApplicationContext applicationContext;
    
    @Override
    public List<Category> listWithArticleCount() {
        List<Category> categories = list();
        
        // 为每个分类统计已发布且已审核的文章数量
        for (Category category : categories) {
            LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Article::getCategoryId, category.getId())
                   .eq(Article::getStatus, 1)  // 只统计已发布的文章
                   .eq(Article::getAuditStatus, 1); // 只统计已审核通过的文章
            long count = articleMapper.selectCount(wrapper);
            category.setCount((int) count);
        }
        
        return categories;
    }
    
    @Override
    public Map<String, Object> getArticlesByCategory(Long categoryId, Integer pageNum, Integer pageSize) {
        // 分页查询文章
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getCategoryId, categoryId)
               .eq(Article::getStatus, 1)
               .eq(Article::getAuditStatus, 1)
               .orderByDesc(Article::getCreateTime);
        Page<Article> articlePage = articleMapper.selectPage(page, wrapper);
        
        // 使用延迟获取ArticleService来填充文章详细信息（避免循环依赖）
        if (!articlePage.getRecords().isEmpty()) {
            var articleService = applicationContext.getBean("articleServiceImpl", 
                com.panyu.mybolg.service.ArticleService.class);
            articleService.fillArticleDetails(articlePage.getRecords());
        }
        
        // 封装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", articlePage.getRecords());
        result.put("total", articlePage.getTotal());
        
        // 查询分类信息
        Category category = getById(categoryId);
        result.put("categoryInfo", category);
        
        return result;
    }
}

package com.panyu.mybolg.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.panyu.mybolg.entity.Article;
import com.panyu.mybolg.entity.Category;

import java.util.List;
import java.util.Map;

public interface CategoryService extends IService<Category> {
    
    /**
     * 获取所有分类并统计文章数量
     */
    List<Category> listWithArticleCount();
    
    /**
     * 获取分类下的文章列表
     */
    Map<String, Object> getArticlesByCategory(Long categoryId, Integer pageNum, Integer pageSize);
}

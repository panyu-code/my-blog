package com.panyu.mybolg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panyu.mybolg.entity.Article;
import com.panyu.mybolg.entity.ArticleTag;
import com.panyu.mybolg.entity.Tag;
import com.panyu.mybolg.mapper.ArticleMapper;
import com.panyu.mybolg.mapper.ArticleTagMapper;
import com.panyu.mybolg.mapper.TagMapper;
import com.panyu.mybolg.service.TagService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
    
    @Resource
    private ArticleMapper articleMapper;
    
    @Resource
    private ArticleTagMapper articleTagMapper;
    
    @Resource
    private ApplicationContext applicationContext;
    
    @Override
    public List<Tag> listWithArticleCount() {
        List<Tag> tags = list();
        
        // 为每个标签统计已发布的文章数量
        for (Tag tag : tags) {
            // 查询标签关联的文章ID
            LambdaQueryWrapper<ArticleTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(ArticleTag::getTagId, tag.getId());
            List<ArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);
            
            if (articleTags.isEmpty()) {
                tag.setCount(0);
            } else {
                List<Long> articleIds = articleTags.stream()
                        .map(ArticleTag::getArticleId)
                        .collect(Collectors.toList());
                
                // 统计已发布的文章数
                LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
                wrapper.in(Article::getId, articleIds)
                       .eq(Article::getStatus, 1); // 只统计已发布的文章
                long count = articleMapper.selectCount(wrapper);
                tag.setCount((int) count);
            }
        }
        
        return tags;
    }
    
    @Override
    public Map<String, Object> getArticlesByTag(Long tagId, Integer pageNum, Integer pageSize) {
        // 先查询该标签下的所有文章ID
        LambdaQueryWrapper<ArticleTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(ArticleTag::getTagId, tagId);
        List<ArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);
        
        List<Long> articleIds = articleTags.stream()
                .map(ArticleTag::getArticleId)
                .collect(Collectors.toList());
        
        // 根据文章ID查询文章列表
        Page<Article> page = new Page<>(pageNum, pageSize);
        
        if (articleIds.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("list", page.getRecords());
            result.put("total", page.getTotal());
            Tag tag = getById(tagId);
            result.put("tagInfo", tag);
            return result;
        }
        
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Article::getId, articleIds)
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
        Tag tag = getById(tagId);
        result.put("tagInfo", tag);
        
        return result;
    }
}

package com.panyu.mybolg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panyu.mybolg.entity.Tag;

import java.util.List;
import java.util.Map;

public interface TagService extends IService<Tag> {
    
    /**
     * 获取所有标签并统计文章数量
     */
    List<Tag> listWithArticleCount();
    
    /**
     * 获取标签下的文章列表
     */
    Map<String, Object> getArticlesByTag(Long tagId, Integer pageNum, Integer pageSize);
}

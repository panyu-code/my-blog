package com.panyu.mybolg.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.panyu.mybolg.entity.Comment;

import java.util.Map;

public interface CommentService extends IService<Comment> {
    
    /**
     * 管理端分页查询评论列表
     */
    Map<String, Object> listAllComments(Integer pageNum, Integer pageSize, Integer status);
    
    /**
     * Web端获取文章评论列表（包含回复）
     */
    Map<String, Object> listArticleComments(Long articleId, Integer pageNum, Integer pageSize);
    
    /**
     * 保存评论（设置默认值）
     */
    Comment saveComment(Comment comment);
    
    /**
     * 增加评论点赞数
     */
    boolean likeComment(Long id);
    
    /**
     * 审核评论
     */
    boolean auditComment(Long id, Integer status);
}

package com.panyu.mybolg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panyu.mybolg.entity.Comment;
import com.panyu.mybolg.entity.User;
import com.panyu.mybolg.mapper.CommentMapper;
import com.panyu.mybolg.service.CommentService;
import com.panyu.mybolg.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    
    @Resource
    private UserService userService;
    
    @Override
    public Map<String, Object> listAllComments(Integer pageNum, Integer pageSize, Integer status) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(Comment::getStatus, status);
        }
        wrapper.orderByDesc(Comment::getCreateTime);
        
        Page<Comment> result = page(page, wrapper);
        
        // 填充用户信息
        fillUserInfo(result.getRecords());
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        
        return data;
    }
    
    @Override
    public Map<String, Object> listArticleComments(Long articleId, Integer pageNum, Integer pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId)
               .eq(Comment::getParentId, 0) // 只查询一级评论
               .eq(Comment::getStatus, 1) // 只查询已通过的评论
               .orderByDesc(Comment::getCreateTime);
        
        Page<Comment> result = page(page, wrapper);
        
        // 填充用户信息
        List<Comment> comments = result.getRecords();
        fillUserInfo(comments);
        
        // 填充回复（子评论）
        fillReplies(comments, articleId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", comments);
        data.put("total", result.getTotal());
        
        return data;
    }
    
    @Override
    public Comment saveComment(Comment comment) {
        // 设置默认状态为已通过
        if (comment.getStatus() == null) {
            comment.setStatus(1);
        }
        // 设置默认parentId
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        // 设置默认likeCount
        if (comment.getLikeCount() == null) {
            comment.setLikeCount(0);
        }
        
        save(comment);
        return comment;
    }
    
    @Override
    public boolean likeComment(Long id) {
        Comment comment = getById(id);
        if (comment == null) {
            return false;
        }
        comment.setLikeCount(comment.getLikeCount() + 1);
        return updateById(comment);
    }
    
    @Override
    public boolean auditComment(Long id, Integer status) {
        if (status == null || (status != 1 && status != 2)) {
            throw new RuntimeException("无效的状态");
        }
        
        Comment comment = getById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        
        comment.setStatus(status);
        return updateById(comment);
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
    
    /**
     * 填充评论的回复（子评论）
     */
    private void fillReplies(List<Comment> comments, Long articleId) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        
        // 收集所有父评论ID
        List<Long> parentIds = comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        
        // 查询所有回复
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId)
               .in(Comment::getParentId, parentIds)
               .eq(Comment::getStatus, 1)
               .orderByAsc(Comment::getCreateTime);
        List<Comment> replies = list(wrapper);
        
        // 填充回复的用户信息
        fillUserInfo(replies);
        
        // 按父评论ID分组
        Map<Long, List<Comment>> repliesMap = replies.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));
        
        // 将回复设置到对应的父评论
        comments.forEach(comment -> {
            List<Comment> commentReplies = repliesMap.get(comment.getId());
            comment.setReplies(commentReplies != null ? commentReplies : List.of());
        });
    }
}

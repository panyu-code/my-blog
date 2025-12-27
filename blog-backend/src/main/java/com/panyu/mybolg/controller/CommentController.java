package com.panyu.mybolg.controller;

import com.panyu.mybolg.common.Result;
import com.panyu.mybolg.entity.Comment;
import com.panyu.mybolg.exception.BusinessException;
import com.panyu.mybolg.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "评论管理", description = "评论相关接口")
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    /**
     * 管理端获取所有评论列表
     */
    @Operation(summary = "评论列表（管理端）", description = "管理端分页获取所有评论")
    @GetMapping("/list")
    public Result<Map<String, Object>> listAll(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "审核状态") @RequestParam(required = false) Integer status) {
        Map<String, Object> data = commentService.listAllComments(pageNum, pageSize, status);
        return Result.success(data);
    }

    /**
     * Web端获取文章评论列表
     */
    @Operation(summary = "文章评论列表", description = "获取指定文章的评论列表")
    @GetMapping("/{articleId}/list")
    public Result<Map<String, Object>> list(
            @Parameter(description = "文章ID") @PathVariable Long articleId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> data = commentService.listArticleComments(articleId, pageNum, pageSize);
        return Result.success(data);
    }

    @Operation(summary = "发表评论", description = "创建新的评论")
    @PostMapping
    public Result<Comment> save(@RequestBody Comment comment) {
        Comment savedComment = commentService.saveComment(comment);
        return Result.success(savedComment);
    }

    @Operation(summary = "回复评论", description = "回复他人的评论")
    @PostMapping("/reply")
    public Result<Comment> reply(@RequestBody Comment comment) {
        Comment savedComment = commentService.saveComment(comment);
        return Result.success(savedComment);
    }

    @Operation(summary = "删除评论", description = "根据ID删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "评论id") @PathVariable Long id) {
        commentService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "点赞评论", description = "评论点赞数+1")
    @PostMapping("/{id}/like")
    public Result<Void> like(@Parameter(description = "评论ID") @PathVariable Long id) {
        boolean success = commentService.likeComment(id);
        if (!success) {
            throw new BusinessException("操作失败");
        }
        return Result.success();
    }

    /**
     * 审核评论
     */
    @Operation(summary = "审核评论", description = "管理员审核评论")
    @PutMapping("/{id}/audit")
    public Result<Void> audit(
            @Parameter(description = "评论ID") @PathVariable Long id, 
            @RequestBody Map<String, Integer> params) {
        Integer status = params.get("status");
        boolean success = commentService.auditComment(id, status);
        if (!success) {
            throw new BusinessException("操作失败");
        }
        return Result.success();
    }
}

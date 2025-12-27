package com.panyu.mybolg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panyu.mybolg.common.Result;
import com.panyu.mybolg.entity.Article;
import com.panyu.mybolg.exception.BusinessException;
import com.panyu.mybolg.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "文章管理", description = "文章相关接口")
@RestController
@RequestMapping("/article")
public class ArticleController {
    
    @Resource
    private ArticleService articleService;

    @Operation(summary = "文章列表", description = "分页查询文章列表，支持标题、分类、状态筛选")
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "文章标题") @RequestParam(required = false) String title,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "状态(0:草稿,1:已发布)") @RequestParam(required = false) Integer status) {
        Page<Article> result = articleService.listWithDetails(pageNum, pageSize, title, categoryId, status);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return Result.success(data);
    }

    @Operation(summary = "获取文章详情", description = "根据ID获取文章详细信息")
    @GetMapping("/{id}")
    public Result<Article> getById(@Parameter(description = "文章ID") @PathVariable Long id) {
        Article article = articleService.getDetailById(id);
        if (article == null) {
            throw new BusinessException(404, "文章不存在");
        }
        return Result.success(article);
    }

    @Operation(summary = "搜索文章", description = "根据关键词搜索文章")
    @GetMapping("/search")
    public Result<Map<String, Object>> search(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Article> result = articleService.searchArticles(keyword, pageNum, pageSize);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return Result.success(data);
    }

    @Operation(summary = "新增文章", description = "创建新的文章")
    @PostMapping
    public Result<Article> save(@RequestBody Article article) {
        boolean success = articleService.saveWithTags(article, article.getTags());
        if (!success) {
            throw new BusinessException("保存失败");
        }
        return Result.success(article);
    }

    @Operation(summary = "更新文章", description = "更新文章信息")
    @PutMapping
    public Result<Article> update(@RequestBody Article article) {
        boolean success = articleService.updateWithTags(article, article.getTags());
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success(article);
    }

    @Operation(summary = "删除文章", description = "根据ID删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "文章ID") @PathVariable Long id) {
        boolean success = articleService.deleteWithRelations(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success();
    }

    @Operation(summary = "增加浏览量", description = "文章浏览量+1")
    @PostMapping("/{id}/view")
    public Result<Void> increaseView(@Parameter(description = "文章ID") @PathVariable Long id) {
        boolean success = articleService.increaseViewCount(id);
        if (!success) {
            throw new BusinessException("操作失败");
        }
        return Result.success();
    }

    @Operation(summary = "点赞文章", description = "文章点赞数+1")
    @PostMapping("/{id}/like")
    public Result<Void> like(@Parameter(description = "文章ID") @PathVariable Long id) {
        boolean success = articleService.increaseLikeCount(id);
        if (!success) {
            throw new BusinessException("操作失败");
        }
        return Result.success();
    }
}

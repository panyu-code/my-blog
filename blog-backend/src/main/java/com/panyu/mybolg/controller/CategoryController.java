package com.panyu.mybolg.controller;

import com.panyu.mybolg.common.Result;
import com.panyu.mybolg.entity.Category;
import com.panyu.mybolg.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "分类管理", description = "文章分类相关接口")
@RestController
@RequestMapping("/category")
public class CategoryController {
    
    @Resource
    private CategoryService categoryService;
    
    @Operation(summary = "分类列表", description = "获取所有分类列表，包含文章数量")
    @GetMapping("/list")
    public Result<List<Category>> list() {
        List<Category> list = categoryService.listWithArticleCount();
        return Result.success(list);
    }
    
    @Operation(summary = "获取分类详情", description = "根据ID获取分类详细信息")
    @GetMapping("/{id}")
    public Result<Category> getById(@Parameter(description = "分类ID") @PathVariable Long id) {
        Category category = categoryService.getById(id);
        return Result.success(category);
    }
    
    @Operation(summary = "新增分类", description = "创建新的文章分类")
    @PostMapping
    public Result<Category> save(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success(category);
    }
    
    @Operation(summary = "更新分类", description = "更新分类信息")
    @PutMapping
    public Result<Category> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success(category);
    }
    
    @Operation(summary = "删除分类", description = "根据ID删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "分类ID") @PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success();
    }
    
    @Operation(summary = "获取分类下的文章", description = "分页获取指定分类下的所有文章")
    @GetMapping("/{categoryId}/articles")
    public Result<Map<String, Object>> getArticlesByCategory(
            @Parameter(description = "分类ID") @PathVariable Long categoryId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = categoryService.getArticlesByCategory(categoryId, pageNum, pageSize);
        return Result.success(result);
    }
}

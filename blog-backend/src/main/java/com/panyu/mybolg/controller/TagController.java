package com.panyu.mybolg.controller;

import com.panyu.mybolg.common.Result;
import com.panyu.mybolg.entity.Tag;
import com.panyu.mybolg.service.TagService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    private TagService tagService;

    @GetMapping("/list")
    public Result<List<Tag>> list() {
        List<Tag> list = tagService.listWithArticleCount();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Tag> getById(@PathVariable Long id) {
        Tag tag = tagService.getById(id);
        return Result.success(tag);
    }

    @PostMapping
    public Result<Tag> save(@RequestBody Tag tag) {
        tagService.save(tag);
        return Result.success(tag);
    }

    @PutMapping
    public Result<Tag> update(@RequestBody Tag tag) {
        tagService.updateById(tag);
        return Result.success(tag);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.removeById(id);
        return Result.success();
    }

    @GetMapping("/{tagId}/articles")
    public Result<Map<String, Object>> getArticlesByTag(@PathVariable Long tagId,
                                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = tagService.getArticlesByTag(tagId, pageNum, pageSize);
        return Result.success(result);
    }
}

package com.panyu.mybolg.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.panyu.mybolg.common.Result;
import com.panyu.mybolg.entity.Settings;
import com.panyu.mybolg.service.SettingsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/settings")
public class SettingsController {

    @Resource
    private SettingsService settingsService;

    @GetMapping("/list")
    public Result<List<Settings>> list() {
        List<Settings> list = settingsService.list();
        return Result.success(list);
    }

    @GetMapping("/{key}")
    public Result<Settings> getByKey(@PathVariable String key) {
        LambdaQueryWrapper<Settings> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Settings::getKey, key);
        Settings settings = settingsService.getOne(wrapper);
        return Result.success(settings);
    }

    @PutMapping
    public Result<Settings> update(@RequestBody Settings settings) {
        settingsService.updateById(settings);
        return Result.success(settings);
    }
}

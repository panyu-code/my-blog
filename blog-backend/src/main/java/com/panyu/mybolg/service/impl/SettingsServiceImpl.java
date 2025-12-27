package com.panyu.mybolg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panyu.mybolg.entity.Settings;
import com.panyu.mybolg.mapper.SettingsMapper;
import com.panyu.mybolg.service.SettingsService;
import org.springframework.stereotype.Service;

@Service
public class SettingsServiceImpl extends ServiceImpl<SettingsMapper, Settings> implements SettingsService {
}

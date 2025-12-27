package com.panyu.mybolg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panyu.mybolg.entity.ArticleTag;
import com.panyu.mybolg.mapper.ArticleTagMapper;
import com.panyu.mybolg.service.ArticleTagService;
import org.springframework.stereotype.Service;

@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag> implements ArticleTagService {
}

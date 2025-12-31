package com.panyu.mybolg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panyu.mybolg.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    
    /**
     * 根据文章ID列表获取评论数量
     */
    List<Map<String, Object>> getCommentCountsByArticleIds(@Param("articleIds") List<Long> articleIds);
}

package com.panyu.mybolg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panyu.mybolg.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}

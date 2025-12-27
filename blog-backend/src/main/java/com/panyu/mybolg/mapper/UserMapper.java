package com.panyu.mybolg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panyu.mybolg.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}

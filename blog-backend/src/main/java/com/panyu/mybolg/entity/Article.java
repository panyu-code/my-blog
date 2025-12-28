package com.panyu.mybolg.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.panyu.mybolg.vo.TagVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String summary;

    private String content;

    private String cover;

    private Long categoryId;

    private Long authorId;

    @TableField(exist = false)
    private String authorName;

    @TableField(exist = false)
    private String authorAvatar;

    @TableField(exist = false)
    private String categoryName;

    // Web端展示使用：包含名称和颜色的标签对象列表
    @TableField(exist = false)
    private List<TagVO> tagList;

    // Admin端编辑使用：标签ID字符串列表
    @TableField(exist = false)
    private List<String> tags;

    private Integer status;

    private Integer auditStatus;

    private String auditReason;

    private Integer isTop;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    @TableLogic
    private Integer deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime publishTime;
}

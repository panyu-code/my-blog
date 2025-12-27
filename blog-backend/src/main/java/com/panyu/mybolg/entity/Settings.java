package com.panyu.mybolg.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("settings")
public class Settings {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String key;

    private String value;

    private String description;

    private String type;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

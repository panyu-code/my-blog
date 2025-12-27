package com.panyu.mybolg.vo;

import lombok.Data;

@Data
public class TagVO {
    private String name;
    private String color;

    public TagVO() {
    }

    public TagVO(String name, String color) {
        this.name = name;
        this.color = color;
    }
}

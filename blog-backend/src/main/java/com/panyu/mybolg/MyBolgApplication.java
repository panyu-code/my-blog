package com.panyu.mybolg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.panyu.mybolg.mapper")
public class MyBolgApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBolgApplication.class, args);
    }
}

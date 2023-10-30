package com.zmj.springboot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.zmj.springboot.mapper")
@SpringBootApplication
public class SpringbootWebJspApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootWebJspApplication.class, args);
    }
}

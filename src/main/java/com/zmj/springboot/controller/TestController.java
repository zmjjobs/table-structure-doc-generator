package com.zmj.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author : zhumengjun
 * @create 2023/3/31 13:02
 */
@RestController
public class TestController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/sql")
    public String queryFromDb(){
        Long aLong = jdbcTemplate.queryForObject("select count(*) from employee",Long.class);
        return aLong.toString();
    }
}

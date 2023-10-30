package com.zmj.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Value("${employee.lastName:无名}")
    private String lastName;
    @Value("${MAVEN_HOME}")
    private String mavenHome;
    @Value("${os.name}")
    private String osName;

    @GetMapping("/msg")
    public String msg(){
        return "Hello " + lastName + ",mavenHome="+mavenHome+",osName="+osName;
    }


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/queryDB")
    public String queryDB(){
        Long ret = jdbcTemplate.queryForObject("select count(*) from employee",Long.class);
        return ret.toString();
    }
}

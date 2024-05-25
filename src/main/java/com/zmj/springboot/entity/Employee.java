package com.zmj.springboot.entity;

import lombok.Data;

/**
 * @Author : zhumengjun
 * @create 2023/3/12 21:53
 */
@Data
public class Employee {
    private Integer id;
    private String lastName;
    private String email;
    private Integer gender;
    private Integer dId;
}

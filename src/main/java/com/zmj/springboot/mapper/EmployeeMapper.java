package com.zmj.springboot.mapper;

import com.zmj.springboot.entity.Employee;

/**
 * @Author : zhumengjun
 * @create 2023/3/12 21:57
 */
public interface EmployeeMapper {
    Employee getEmp(Integer id);
}

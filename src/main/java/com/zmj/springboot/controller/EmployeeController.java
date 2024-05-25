package com.zmj.springboot.controller;

import com.zmj.springboot.entity.Employee;
import com.zmj.springboot.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author : zhumengjun
 * @create 2023/3/12 22:10
 */
@RestController
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/getEmp/{id}")
    public Employee getEmpById(@PathVariable("id") Integer id){
        Employee emp = employeeService.getEmpById(id);
        return emp;
    }
}

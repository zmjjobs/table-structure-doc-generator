package com.zmj.springboot.service;

import com.zmj.springboot.entity.Employee;
import com.zmj.springboot.mapper.EmployeeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author : mjzhud
 * @create 2023/3/12 22:12
 */
@Service
public class EmployeeService {
    @Autowired
    private EmployeeMapper employeeMapper;

    public Employee getEmpById(Integer id){
        Employee emp = employeeMapper.getEmp(id);
        return emp;
    }
}

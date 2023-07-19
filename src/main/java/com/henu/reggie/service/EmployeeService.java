package com.henu.reggie.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.henu.reggie.entity.Employee;
import com.henu.reggie.entity.Result;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public interface EmployeeService extends IService<Employee> {
    Result<Employee> login(Employee employee);

    Result<Page> pageSelect(int page, int pageSize, String name);

    Result<Employee> insert(HttpServletRequest request,Employee employee);

    Result<Employee> update(HttpServletRequest request, Employee employee);
}

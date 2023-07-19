package com.henu.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.henu.reggie.entity.Employee;
import com.henu.reggie.entity.Result;
import com.henu.reggie.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 登陆
     * @param httpServletRequest
     * @param employee
     * @return
     */
    @PostMapping("login")
    public Result<Employee> login(HttpServletRequest httpServletRequest, @RequestBody Employee employee){
        Result<Employee> result = employeeService.login(employee);
        if (result.getCode() == 1){
            Employee data = result.getData();
            httpServletRequest.getSession().setAttribute("employee",data.getId());
        }
        return result;
    }

    /**
     * 退出
     * @param request
     * @return
     */
    @PostMapping("logout")
    public Result<Employee> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return Result.success(null);
    }

    /**
     * 分页查询员工
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> pageSelect(int page,int pageSize, String name){
        Result<Page> result = employeeService.pageSelect(page,pageSize,name);
        return result;
    }

    /**
     * 新增员工
     * @param employee
     * @param request
     * @return
     */
    @PostMapping
    public Result<Employee> insert(@RequestBody Employee employee,HttpServletRequest request){
        return employeeService.insert(request,employee);
    }

    /**
     * 更新员工状态
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public Result<Employee> update(HttpServletRequest request, @RequestBody Employee employee){
        return employeeService.update(request,employee);
    }

    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        if (employee != null){
            return Result.success(employee);
        }
        return Result.error("没有找到数据");
    }
}

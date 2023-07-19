package com.henu.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.henu.reggie.entity.Employee;
import com.henu.reggie.entity.Result;
import com.henu.reggie.mapper.EmployeeMapper;
import com.henu.reggie.service.EmployeeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 登录
     * @param employee
     * @return
     */
    @Override
    public Result<Employee> login(Employee employee) {
        //1.密码加密
        String pwd = employee.getPassword();
        pwd = DigestUtils.md5DigestAsHex(pwd.getBytes(StandardCharsets.UTF_8));
        //2.根据员工名查询
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Employee::getUsername,employee.getUsername());
        Employee employee1 = employeeMapper.selectOne(lqw);

        //3，4,5.判断员工和密码,判断员工状态
        if (employee1 == null){
            return Result.error("没有该用户");
        }

        if (!pwd.equals(employee1.getPassword())){
            return Result.error("密码错误");
        }

        if (employee1.getStatus() == 0){
            return Result.error("员工已被被禁用");
        }

        //6登陆成功
        return Result.success(employee1);
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public Result<Page> pageSelect(int page, int pageSize, String name) {
        Page page1 = new Page(page,pageSize);
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper();
        lqw.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        lqw.orderByDesc(Employee::getUpdateTime);
        employeeMapper.selectPage(page1, lqw);
        return Result.success(page1);
    }

    /**
     * 添加员工
     * @param request
     * @param employee
     * @return
     */
    @Override
    public Result<Employee> insert(HttpServletRequest request,Employee employee) {
        //加密密码
        String pwd = "123456";
        employee.setPassword(DigestUtils.md5DigestAsHex(pwd.getBytes(StandardCharsets.UTF_8)));

        /*employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);*/
        employeeMapper.insert(employee);

        return Result.success(null);
    }

    /**
     * 修改员工状态
     * @param employee
     * @return
     */
    @Override
    public Result<Employee> update(HttpServletRequest request, Employee employee) {
        /*Long updateId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateUser(updateId);
        employee.setUpdateTime(LocalDateTime.now());*/
        employeeMapper.updateById(employee);

        return Result.success(null);
    }
}

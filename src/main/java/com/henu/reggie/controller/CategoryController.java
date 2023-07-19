package com.henu.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.henu.reggie.entity.Category;
import com.henu.reggie.entity.Result;
import com.henu.reggie.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;


    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @GetMapping("/list")
    public Result<List<Category>> getCategoryByType(Integer type){
        if (type == null){
            return Result.success(categoryService.list());
        }
        return categoryService.getCategoryByType(type);
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping("/page")
    public Result<Page> pageSelect(int page,int pageSize){
        return categoryService.pageSelect(page,pageSize);
    }

    /**
     * 添加菜品、套餐分类
     * @param category
     * @return
     */
    @PostMapping
    public Result<Category> insert(@RequestBody Category category){
        return categoryService.insert(category);
    }

    /**
     * 修改
     * @param category
     * @return
     */
    @PutMapping
    public Result<Category> update(@RequestBody Category category){
        categoryService.updateById(category);
        return Result.success(null);
    }

    /**
     * 删除分类（判断是否关联）
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<Category> delete(Long ids){

        return categoryService.delete(ids);
    }
}

package com.henu.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.henu.reggie.common.CustomException;
import com.henu.reggie.entity.Category;
import com.henu.reggie.entity.Dish;
import com.henu.reggie.entity.Result;
import com.henu.reggie.entity.Setmeal;
import com.henu.reggie.mapper.CategoryMapper;
import com.henu.reggie.mapper.DishMapper;
import com.henu.reggie.mapper.SetmealMapper;
import com.henu.reggie.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Result<Page> pageSelect(int page, int pageSize) {
        IPage<Category> iPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.orderByAsc(Category::getSort);
        categoryMapper.selectPage(iPage,lqw);
        return Result.success((Page) iPage);
    }

    /**
     * 添加菜品、套餐分类
     * @param category
     * @return
     */
    @Override
    public Result<Category> insert(Category category) {
        categoryMapper.insert(category);
        return Result.success(null);
    }

    /**
     * 删除（判断是否关联）
     * @param id
     * @return
     */
    @Override
    public Result<Category> delete(Long id) {
        //1.菜品分类
        LambdaQueryWrapper<Dish> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(Dish::getCategoryId,id);
        Integer count1 = dishMapper.selectCount(lqw1);
        if(count1 > 0){
            throw new CustomException("当前分类关联了菜品，不能删除");
        }
        //2.套餐分类
        LambdaQueryWrapper<Setmeal> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(Setmeal::getCategoryId,id);
        Integer count2 = setmealMapper.selectCount(lqw2);
        if (count2 > 0){
            throw new CustomException("当前分类关联了菜品，不能删除");
        }
        categoryMapper.deleteById(id);

        return Result.success(null);
    }

    /**
     * 查询菜品、套餐分类
     * @param type
     * @return
     */
    @Override
    public Result<List<Category>> getCategoryByType(int type) {
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Category::getType,type);
        List<Category> list = categoryMapper.selectList(lqw);
        return Result.success(list);
    }
}

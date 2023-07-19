package com.henu.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.henu.reggie.common.CustomException;
import com.henu.reggie.dto.DishDto;
import com.henu.reggie.entity.*;
import com.henu.reggie.mapper.DishMapper;
import com.henu.reggie.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService  setmealDishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 菜品分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public Result<Page> pageSelect(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<com.henu.reggie.dto.DishDto> pageDtoInfo = new Page<>(page,pageSize);

        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(name), Dish::getName,name);
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        dishMapper.selectPage(pageInfo,lqw);

        BeanUtils.copyProperties(pageInfo,pageDtoInfo,"records");
        List<Dish> records = pageInfo.getRecords();

        List<com.henu.reggie.dto.DishDto> dishDtoList = records.stream().map(item -> {
            Category category = categoryService.getById(item.getCategoryId());
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            dishDto.setCategoryName(category.getName());
            return dishDto;
        }).collect(Collectors.toList());

        pageDtoInfo.setRecords(dishDtoList);

        return Result.success(pageDtoInfo);
    }

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @Transactional
    @Override
    public Result<Dish> insert(com.henu.reggie.dto.DishDto dishDto) {
        dishMapper.insert(dishDto);
        List<DishFlavor> flavorList = dishDto.getFlavors();
        Long dishId = dishDto.getId();
        List<DishFlavor> list = flavorList.stream().map(item -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(list);

        return Result.success(null);
    }

    /**
     * 查找菜品
     * @param dishId
     * @return
     */
    @Override
    public Result<com.henu.reggie.dto.DishDto> GetById(Long dishId) {
        Dish dish = dishMapper.selectById(dishId);
        com.henu.reggie.dto.DishDto dishDto = new com.henu.reggie.dto.DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,dishId);
        List<DishFlavor> flavorList = dishFlavorService.list(lqw);

        dishDto.setFlavors(flavorList);

        return Result.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @Transactional
    @Override
    public Result<com.henu.reggie.dto.DishDto> updateWithFlavor(com.henu.reggie.dto.DishDto dishDto) {
        dishMapper.updateById(dishDto);

        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(lqw);

        List<DishFlavor> flavors = dishDto.getFlavors();
        List<DishFlavor> list = flavors.stream().map(item -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(list);


        return Result.success(null);
    }

    /**
     * （批量）停售
     * @param ids
     * @return
     */
    @Override
    public Result<String> haltTheSalesDish(List<Long> ids) {
        //根据菜品id得到所关联的套餐菜品信息
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.in(SetmealDish::getDishId,ids);
        List<SetmealDish> setmealDishList = setmealDishService.list(lqw);

        //若菜品没有关联套餐，可以停售
        if (setmealDishList.size() == 0){
            List<Dish> dishList1 = updateDishStatusList(ids);
            this.updateBatchById(dishList1);
            return Result.success("修改成功");
        }

        //得到菜品所关联的套餐id集合
        Set<Long> setmealDishIds = new TreeSet<>();
        for (SetmealDish setmealDish : setmealDishList
             ) {
            setmealDishIds.add(setmealDish.getSetmealId());
        }

        //得到菜品所关联的套餐集合
        List<Setmeal> setmealList = setmealService.listByIds(setmealDishIds);

        //若关联的套餐正在售卖，无法停售菜品
        for (Setmeal s :
                setmealList) {
            if(s.getStatus() == 1){
                throw new CustomException("菜品关联的套餐正在售卖中，无法停售");
            }
        }

        //关联的套餐已停售，乐意停售菜品
        List<Dish> dishList2 = updateDishStatusList(ids);
        this.updateBatchById(dishList2);
        return Result.success("修改成功");
    }

    @Override
    public Result<String> delete(List<Long> ids) {
        //菜品是否关联套餐
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.in(SetmealDish::getDishId,ids);
        int count = setmealDishService.count(lqw);

        //关联套餐，不能删除
        if (count > 0){
            throw new CustomException("菜品关联套餐，不能删除");
        }

        //没有关联套餐,可以删除
        dishMapper.deleteBatchIds(ids);
        return Result.success("删除成功");
    }

    /**
     * 得到带id和修改后状态的菜品集合
     * @param ids
     * @return
     */
    private List<Dish> updateDishStatusList(List<Long> ids){
        List<Dish> list = new ArrayList<>();
        for (Long id :
                ids) {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(0);
            list.add(dish);
        }
        return list;
    }
}

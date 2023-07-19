package com.henu.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.henu.reggie.dto.SetmealDto;
import com.henu.reggie.entity.Dish;
import com.henu.reggie.entity.Result;
import com.henu.reggie.entity.Setmeal;
import com.henu.reggie.entity.SetmealDish;
import com.henu.reggie.service.DishService;
import com.henu.reggie.service.SetmealDishService;
import com.henu.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;


    @GetMapping("/dish/{id}")
    public Result<List<SetmealDish>> getOne(@PathVariable Long id){
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(lqw);

        //获取图片
        List<SetmealDish> collect = list.stream().map(item -> {
            Dish dish = dishService.getById(item.getDishId());
            item.setImage(dish.getImage());
            return item;
        }).collect(Collectors.toList());

        return Result.success(collect);
    }


    /**
     * 移动端得到套餐列表
     * @param setmealDto
     * @return
     */
    @GetMapping("/list")
    public Result<List<SetmealDto>> getSetmealList(SetmealDto setmealDto){
        return setmealService.getSetmealList(setmealDto);
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> pageSelect(int page,int pageSize,String name){
        return setmealService.pageSelect(page,pageSize,name);
    }

    /**
     * 添加套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public Result<SetmealDto> insert(@RequestBody SetmealDto setmealDto){
        return setmealService.insert(setmealDto);
    }

    /**
     * 根据ID得到套餐信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SetmealDto> getWithDish(@PathVariable Long id){
        return setmealService.getSetMealById(id);
    }

    /**
     * 修改套餐（带所属菜品）
     * @param setmealDto
     * @return
     */
    @PutMapping
    public Result<SetmealDto> updateWithDish(@RequestBody SetmealDto setmealDto){
        return setmealService.updateWithDish(setmealDto);
    }

    /**
     * （批量）停售
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public Result<String> haltTheSales(@RequestParam List<Long> ids){
        List<Setmeal> list = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(ids.get(i));
            setmeal.setStatus(0);
            list.add(setmeal);
        }
        setmealService.updateBatchById(list);
        return Result.success("修改成功");
    }

    /**
     * （批量）启售
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public Result<String> launchForSelling(@RequestParam List<Long> ids){
        List<Setmeal> list = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(ids.get(i));
            setmeal.setStatus(1);
            list.add(setmeal);
        }
        setmealService.updateBatchById(list);
        return Result.success("修改成功");
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids){
        return setmealService.delete(ids);
    }




}

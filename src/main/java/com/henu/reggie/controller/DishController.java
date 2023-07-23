package com.henu.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.henu.reggie.dto.DishDto;
import com.henu.reggie.entity.Category;
import com.henu.reggie.entity.Dish;
import com.henu.reggie.entity.DishFlavor;
import com.henu.reggie.entity.Result;
import com.henu.reggie.service.CategoryService;
import com.henu.reggie.service.DishFlavorService;
import com.henu.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 菜品分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @RequestMapping("/page")
    public Result<Page> page(int page,int pageSize,String name){
        return dishService.pageSelect(page,pageSize,name);
    }


    /**
     * 新建菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public Result<Dish> insert(@RequestBody com.henu.reggie.dto.DishDto dishDto){
        return dishService.insert(dishDto);
    }

    /**
     * 查找菜品(带标签）
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<com.henu.reggie.dto.DishDto> getById(@PathVariable Long id){
        return dishService.GetById(id);
    }

    /**
     * 更改菜品（带标签）
     * @param dishDto
     * @return
     */
    @PutMapping
    public Result<com.henu.reggie.dto.DishDto> updateWithFlavor(@RequestBody com.henu.reggie.dto.DishDto dishDto){
        return dishService.updateWithFlavor(dishDto);
    }

    /**
     * （批量）停售
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public Result<String> haltTheSalesDish(@RequestParam List<Long> ids){
        return dishService.haltTheSalesDish(ids);
    }

    /**
     * （批量）启售
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public Result<String> launchForSelling(@RequestParam List<Long> ids){
        List<Dish> list = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Dish dish = new Dish();
            dish.setId(ids.get(i));
            dish.setStatus(1);
            list.add(dish);
        }
        dishService.updateBatchById(list);
        return Result.success("修改成功");
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids){
        return dishService.delete(ids);
    }


    /**
     * 查找菜品（不带标签）
     * @param dish
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "dishCache",key = "#dish.categoryId + '_' + #dish.status")
    public Result<List<DishDto>> getDish(Dish dish){
        Long categoryId = dish.getCategoryId();
        List<DishDto> dishDtoList = null;
        /*String key = "dish_" + categoryId + "_" + dish.getStatus();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //Redis中有缓存的数据
        if (dishDtoList != null){
            return Result.success(dishDtoList);
        }*/

        //Redis中没有缓存的数据
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(categoryId != null, Dish::getCategoryId,categoryId);
        lqw.eq(Dish::getStatus,"1");
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(lqw);

        dishDtoList = dishList.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            Category category = categoryService.getById(item.getCategoryId());
            dishDto.setCategoryName(category.getName());

            LambdaQueryWrapper<DishFlavor> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> dishFlavors = dishFlavorService.list(lqw2);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());

        //将查到的数据存入缓存中
        //redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);
        return Result.success(dishDtoList);
    }


}

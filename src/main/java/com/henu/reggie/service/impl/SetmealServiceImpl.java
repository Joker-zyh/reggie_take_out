package com.henu.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.henu.reggie.common.CustomException;
import com.henu.reggie.dto.SetmealDto;
import com.henu.reggie.entity.Category;
import com.henu.reggie.entity.Result;
import com.henu.reggie.entity.Setmeal;
import com.henu.reggie.entity.SetmealDish;
import com.henu.reggie.mapper.SetmealMapper;
import com.henu.reggie.service.CategoryService;
import com.henu.reggie.service.SetmealDishService;
import com.henu.reggie.service.SetmealService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Result<Page> pageSelect(int page, int pageSize, String name) {
        //得到Setmeal分页信息
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(name),Setmeal::getName,name);
        lqw.orderByDesc(Setmeal::getUpdateTime);
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        setmealMapper.selectPage(pageInfo,lqw);

        //创建SetmealDto分页，讲pageInfo的信息除了要处理的集合外，复制给setmealDtoPage
        Page<SetmealDto> setmealDtoPage = new Page<>(page,pageSize);
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");

        //创建setmealDtoList集合，将查询出来的setmeal信息复制给setmealDto，并将分类名称添加到setmealDto里（先查出来）
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> setmealDtoList = records.stream().map(item -> {
            Category category = categoryService.getById(item.getCategoryId());
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            setmealDto.setCategoryName(category.getName());
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoList);

        return Result.success(setmealDtoPage);
    }

    /**
     * 新增套餐（带菜品）
     * @param setmealDto
     * @return
     */
    @Transactional
    @Override
    @CacheEvict(value = "setmealCache",key = "#setmealDto.categoryId + '_1'")
    public Result<SetmealDto> insert(SetmealDto setmealDto) {
        //新增套餐
        setmealMapper.insert(setmealDto);

        //在套餐菜品中添加套餐id
        List<SetmealDish> list = setSetmealId(setmealDto);
        //在套餐菜品表添加数据
        setmealDishService.saveBatch(list);

        //修改时删除缓存数据
        String key = "setMeal_" + setmealDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return Result.success(null);
    }

    /**
     * 根据ID得到套餐信息
     * @param id
     * @return
     */
    @Override
    public Result<SetmealDto> getSetMealById(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> setmealDishList = setmealDishService.list(lqw);

        setmealDto.setSetmealDishes(setmealDishList);

        return Result.success(setmealDto);
    }

    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @Transactional
    @Override
    @CacheEvict(value = "setmealCache",key = "#setmealDto.categoryId + '_1'")
    public Result<SetmealDto> updateWithDish(SetmealDto setmealDto) {
        setmealMapper.updateById(setmealDto);

        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(lqw);

        List<SetmealDish> list = setSetmealId(setmealDto);
        setmealDishService.saveBatch(list);

        //修改时删除缓存数据
        String key = "setMeal_" + setmealDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return Result.success(null);
    }

    /**
     * （批量）删除
     * @param ids
     * @return
     */
    @Transactional
    @Override
    @CacheEvict(value = "setmealCache",allEntries = true)
    public Result<String> delete(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId,ids);
        lqw.eq(Setmeal::getStatus,1);
        Integer count = setmealMapper.selectCount(lqw);

        if(count > 0){
            throw new CustomException("商品正在售卖，无法删除。停售后可删除。");
        }

        setmealMapper.deleteBatchIds(ids);

        LambdaQueryWrapper<SetmealDish> lqw2 = new LambdaQueryWrapper<>();
        lqw2.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lqw2);

        return Result.success("删除成功");
    }

    /**
     * 查找所有套餐信息
     * @param setmealDto
     * @return
     */
    @Override
    @Cacheable(value = "setmealCache",key = "#setmealDto.categoryId + '_' + #setmealDto.status")
    public Result<List<SetmealDto>> getSetmealList(SetmealDto setmealDto) {
        String key = "setMeal_" + setmealDto.getCategoryId() + "_" + setmealDto.getStatus();

        /*List<SetmealDto> setmealDtoList;
        setmealDtoList = (List<SetmealDto>) redisTemplate.opsForValue().get(key);
        if (setmealDtoList != null){
            return Result.success(setmealDtoList);
        }*/

        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Setmeal::getCategoryId,setmealDto.getCategoryId());
        lqw.eq(Setmeal::getStatus,setmealDto.getStatus());
        lqw.orderByAsc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealMapper.selectList(lqw);

        List<SetmealDto> setmealDtoList = setmealList.stream().map(item -> {
            SetmealDto setmealDto1 = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto1);

            LambdaQueryWrapper<SetmealDish> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(SetmealDish::getSetmealId, setmealDto.getId());
            List<SetmealDish> setmealDishList = setmealDishService.list(lqw2);
            setmealDto1.setSetmealDishes(setmealDishList);

            return setmealDto1;
        }).collect(Collectors.toList());

        //redisTemplate.opsForValue().set(key,setmealDtoList,60, TimeUnit.MINUTES);
        return Result.success(setmealDtoList);
    }

    /**
     * 在套餐菜品中添加套餐id
     * @param setmealDto
     * @return
     */
    private List<SetmealDish> setSetmealId(SetmealDto setmealDto){
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        List<SetmealDish> list = setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        return list;
    }

}

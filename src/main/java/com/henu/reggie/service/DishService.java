package com.henu.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.henu.reggie.entity.Dish;
import com.henu.reggie.entity.Result;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DishService extends IService<Dish> {
    Result<Page> pageSelect(int page, int pageSize, String name);

    Result<Dish> insert(com.henu.reggie.dto.DishDto dishDto);

    Result<com.henu.reggie.dto.DishDto> GetById(Long dishId);

    Result<com.henu.reggie.dto.DishDto> updateWithFlavor(com.henu.reggie.dto.DishDto dishDto);

    Result<String> haltTheSalesDish(List<Long> ids);

    Result<String> delete(List<Long> ids);
}

package com.henu.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.henu.reggie.dto.SetmealDto;
import com.henu.reggie.entity.Result;
import com.henu.reggie.entity.Setmeal;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SetmealService extends IService<Setmeal>  {
    Result<Page> pageSelect(int page, int pageSize, String name);

    Result<SetmealDto> insert(SetmealDto setmealDto);

    Result<SetmealDto> getSetMealById(Long id);

    Result<SetmealDto> updateWithDish(SetmealDto setmealDto);

    Result<String> delete(List<Long> ids);

    Result<List<SetmealDto>> getSetmealList(SetmealDto setmealDto);
}

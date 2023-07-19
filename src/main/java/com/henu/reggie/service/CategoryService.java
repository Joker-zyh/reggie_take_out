package com.henu.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.henu.reggie.entity.Category;
import com.henu.reggie.entity.Result;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService extends IService<Category> {
    Result<Page> pageSelect(int page, int pageSize);

    Result<Category> insert(Category category);

    Result<Category> delete(Long id);

    Result<List<Category>> getCategoryByType(int type);
}

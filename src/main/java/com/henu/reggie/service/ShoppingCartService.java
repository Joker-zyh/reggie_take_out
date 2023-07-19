package com.henu.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.henu.reggie.entity.Result;
import com.henu.reggie.entity.ShoppingCart;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ShoppingCartService extends IService<ShoppingCart> {
    Result<ShoppingCart> add(ShoppingCart shoppingCart);

    Result<List<ShoppingCart>> getAllInfo();

    Result<String> deleteByUserId();

    Result<String> sub(ShoppingCart shoppingCart);
}

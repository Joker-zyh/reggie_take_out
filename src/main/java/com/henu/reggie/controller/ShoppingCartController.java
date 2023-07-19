package com.henu.reggie.controller;

import com.henu.reggie.entity.Result;
import com.henu.reggie.entity.ShoppingCart;
import com.henu.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加菜品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        return shoppingCartService.add(shoppingCart);
    }

    /**
     * 获取用户购物车信息
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> getAllInfo(){
        return shoppingCartService.getAllInfo();
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public Result<String> deleteByUserId(){
        return shoppingCartService.deleteByUserId();
    }

    /**
     * 减少菜品份数
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public Result<String> sub(@RequestBody ShoppingCart shoppingCart){
        return shoppingCartService.sub(shoppingCart);
    }

}

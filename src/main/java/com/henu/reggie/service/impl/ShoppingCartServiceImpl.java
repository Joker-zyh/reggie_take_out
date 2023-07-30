package com.henu.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.henu.reggie.common.BaseContext;
import com.henu.reggie.entity.Result;
import com.henu.reggie.entity.ShoppingCart;
import com.henu.reggie.mapper.ShoppingCartMapper;
import com.henu.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 添加菜品
     * @param shoppingCart
     * @return
     */
    @Override
    public Result<ShoppingCart> add(ShoppingCart shoppingCart) {
        //判断数据库购物车中该用户是否已选该菜品
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        shoppingCart.setCreateTime(LocalDateTime.now());

        ShoppingCart shoppingCart2 = shoppingCartMapperSelectOne(shoppingCart);

        //如果找到就数量加一，没有找到就新增（数量默认是一）
        if (shoppingCart2 != null){
            shoppingCart2.setNumber(shoppingCart2.getNumber() + 1);
            shoppingCartMapper.updateById(shoppingCart2);
        }else {
            shoppingCart.setNumber(1);
            shoppingCartMapper.insert(shoppingCart);
            shoppingCart2 = shoppingCart;
        }
        return Result.success(shoppingCart2);
    }

    /**
     * 获取用户购物车信息
     * @return
     */
    @Override
    public Result<List<ShoppingCart>> getAllInfo() {
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,currentId);
        lqw.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectList(lqw);
        return Result.success(shoppingCarts);
    }

    /**
     * 清空购物车
     * @return
     */
    @Override
    public Result<String> deleteByUserId() {
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,currentId);
        shoppingCartMapper.delete(lqw);

        return Result.success("清空成功");
    }

    /**
     * 减少菜品份数
     * @param shoppingCart
     * @return
     */
    @Override
    public Result<String> sub(ShoppingCart shoppingCart) {
        //找到购物车中的菜品
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        ShoppingCart selectOne = shoppingCartMapperSelectOne(shoppingCart);

        //判断数量，如果数量大于1就更新，否则就删除
        if (selectOne.getNumber() > 1){
            selectOne.setNumber(selectOne.getNumber() - 1);
            shoppingCartMapper.updateById(selectOne);
        }else {
            shoppingCartMapper.deleteById(selectOne);
        }

        return Result.success("份数减少成功");
    }

    private ShoppingCart shoppingCartMapperSelectOne(ShoppingCart shoppingCart){
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,shoppingCart.getUserId());

        if (shoppingCart.getDishId() != null){
            lqw.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else {
            lqw.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        return shoppingCartMapper.selectOne(lqw);
    }

}

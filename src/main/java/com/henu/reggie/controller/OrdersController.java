package com.henu.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.henu.reggie.common.BaseContext;
import com.henu.reggie.dto.OrdersDto;
import com.henu.reggie.entity.OrderDetail;
import com.henu.reggie.entity.Orders;
import com.henu.reggie.entity.Result;
import com.henu.reggie.entity.ShoppingCart;
import com.henu.reggie.service.OrderDetailService;
import com.henu.reggie.service.OrdersService;
import com.henu.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/again")
    public Result<String> again(@RequestBody Orders orders){
        //得到订单id，根据订单id查询订单明细表
        LambdaQueryWrapper<OrderDetail> lqw = new LambdaQueryWrapper<>();
        lqw.eq(OrderDetail::getOrderId,orders.getId());
        List<OrderDetail> orderDetails = orderDetailService.list(lqw);

        List<ShoppingCart> shoppingCarts = orderDetails.stream().map(item -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(item, shoppingCart);
            return shoppingCart;
        }).collect(Collectors.toList());

        for (ShoppingCart sc :
                shoppingCarts) {
            shoppingCartService.add(sc);
        }

        return Result.success("操作成功");
    }


    @PutMapping
    public Result<String> updateStatus(@RequestBody Orders orders){
        ordersService.updateById(orders);
        return Result.success("派送成功");
    }

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        ordersService.submit(orders);
        return Result.success("下单成功");
    }

    @GetMapping("/page")
    public Result<Page> pageSelect(int page, int pageSize, Integer number, LocalDateTime beginTime,LocalDateTime endTime){
        Page<Orders> pageInfo = new Page<>(page,pageSize);

        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.like(null != number, Orders::getNumber,number);
        lqw.between(null != beginTime,Orders::getOrderTime,beginTime,endTime);
        lqw.orderByDesc(Orders::getOrderTime);

        ordersService.page(pageInfo,lqw);

        return Result.success(pageInfo);


    }

    @GetMapping("/userPage")
    public Result<Page> frontPageSelect(int page,int pageSize){
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Long currentId = BaseContext.getCurrentId();
        Page<OrdersDto> pageInfo2 = new Page<>(page,pageSize);

        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Orders::getUserId,currentId);
        lqw.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, lqw);

        BeanUtils.copyProperties(pageInfo,pageInfo2,"records");

        List<Orders> records = pageInfo.getRecords();

        List<OrdersDto> collect = records.stream().map(item -> {
            LambdaQueryWrapper<OrderDetail> lqw2 = new LambdaQueryWrapper<>();
            lqw2.eq(OrderDetail::getOrderId, item.getId());
            List<OrderDetail> orderDetails = orderDetailService.list(lqw2);

            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            ordersDto.setOrderDetails(orderDetails);
            return ordersDto;
        }).collect(Collectors.toList());

        pageInfo2.setRecords(collect);


        return Result.success(pageInfo2);
    }
}

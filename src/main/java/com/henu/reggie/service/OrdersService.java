package com.henu.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.henu.reggie.entity.Orders;
import org.springframework.stereotype.Service;

@Service
public interface OrdersService extends IService<Orders> {
    void submit(Orders orders);
}

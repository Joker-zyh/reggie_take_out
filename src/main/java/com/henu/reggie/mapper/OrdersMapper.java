package com.henu.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.henu.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}

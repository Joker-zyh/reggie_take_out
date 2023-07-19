package com.henu.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.henu.reggie.entity.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper extends BaseMapper<User> {
}

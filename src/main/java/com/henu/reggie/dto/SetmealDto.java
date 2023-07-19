package com.henu.reggie.dto;


import com.henu.reggie.entity.Setmeal;
import com.henu.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}

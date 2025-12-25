package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        //菜品表添加1条数据
        Dish dish = new Dish();

        BeanUtils.copyProperties(dishDTO, dish);//属性拷贝

        dishMapper.insert(dish);

        //获取insert生成的id
        Long id = dish.getId();

        //菜品口味表添加多条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()){
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(id);
            }
            dishFlavorMapper.insertBatch(flavors);
        }


    }
}

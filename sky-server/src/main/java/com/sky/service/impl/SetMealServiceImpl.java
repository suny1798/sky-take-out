package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetMealServiceImpl implements SetMealService {

    @Autowired
    private SetmealMapper  setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /* *
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void insertSetMealWithDish(SetmealDTO setmealDTO) {
        //套餐表 添加1条数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insertSetMealWithDish(setmeal);

        //获取 insert生成的id
        Long id = setmeal.getId();

        //套餐菜品表添加n条数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if ( setmealDishes != null && !setmealDishes.isEmpty()){
             setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(id);
            });
             setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        ids.forEach( id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        ids.forEach(  id -> {
            setmealMapper.deleteById(id);
            setmealDishMapper.deleteBySetmealId(id);

        });
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);

        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal  setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //  修改套餐
        setmealMapper.update(setmeal);

        //获取套餐ID
        Long id = setmealDTO.getId();

        //  删除套餐对应的口味数据
        setmealDishMapper.deleteBySetmealId(id);

        //  重新插入套餐的口味数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if ( setmealDishes != null && !setmealDishes.isEmpty()){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(id);
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal  setmeal = setmealMapper.getById(id);
        setmeal.setStatus(status);
        setmealMapper.update(setmeal);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}

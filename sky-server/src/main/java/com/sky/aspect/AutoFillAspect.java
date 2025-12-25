package com.sky.aspect;

/*
* 自定义拦截器
*
* */

import com.sky.annatation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /*
    * 1.定义切点
    *
    */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && " +
            "@annotation(com.sky.annatation.AutoFill)")
    public void autoFillPointCut() {}

    /*
    * 2.定义方法，前置拦截
    *
    */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始进行公共字段填充");
        //获取操作类型----insert or update
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//获取方法签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获取方法上的数据库操作类型
        OperationType value = autoFill.value();//获取操作类型
        log.info("操作类型为：{}",value);

        //获取方法参数----实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object obj = args[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据对应的操作类型,为对应的属性赋值
        if (value == OperationType.INSERT) {
            //为插入操作的字段赋值
            //setCreateTime(now)
            //setUpdateTime(now)
            //setCreateUser(currentId)
            //setUpdateUser(currentId)
            Method setCreateTime = obj.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setUpdateTime = obj.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setCreateUser = obj.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateUser = obj.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            //通过反射赋值
            setCreateTime.invoke(obj,now);
            setUpdateTime.invoke(obj,now);
            setCreateUser.invoke(obj,currentId);
            setUpdateUser.invoke(obj,currentId);
        }else if (value == OperationType.UPDATE) {
            //为更新操作的字段赋值
            //setUpdateTime(now)
            //setUpdateUser(currentId)
            Method setUpdateTime = obj.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = obj.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            setUpdateTime.invoke(obj,now);
            setUpdateUser.invoke(obj,currentId);

        }

    }

}

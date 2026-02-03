package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);


    void insert(User user);

    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    @Select("select count(*) from user where create_time < #{localEndTime}")
    Double searchUserByEndTime(LocalDateTime localEndTime);

    @Select("select count(*) from user where create_time between #{localBeginTime} and #{localEndTime}")
    Double searchUserBycreateTime(LocalDateTime localBeginTime, LocalDateTime localEndTime);
}

package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer toBeConfirmed);

    /**
     * 查询
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> processTimeOutOrder(Integer status, LocalDateTime orderTime);

    /**
     * 查询配送中且超时的订单
     * @param deliveryInProgress
     * @param localDateTime
     * @return
     */
    @Select("select * from orders where status = #{deliveryInProgress} and update_time < #{localDateTime}")
    List<Orders> processCancelOrder(Integer deliveryInProgress, LocalDateTime localDateTime);

    /**
     * 根据日期范围查询已完成订单的总金额
     * @param localBeginTime
     * @param localEndTime
     * @param completed
     * @return
     */
    @Select("select sum(amount) from orders where order_time between #{localBeginTime} and #{localEndTime} and status = #{completed}")
    Double searchAmountByDate(LocalDateTime localBeginTime, LocalDateTime localEndTime, Integer completed);

    /**
     * 根据日期范围查询已完成订单的总数量
     * @param localBeginTime
     * @param localEndTime
     * @param completed
     * @return
     */
    @Select("select count(id) from orders where order_time between #{localBeginTime} and #{localEndTime} and status = #{completed}")
    Integer searchByDate(LocalDateTime localBeginTime, LocalDateTime localEndTime, Integer completed);

    @Select("select count(id) from orders where order_time between #{localBeginTime} and #{localEndTime}")
    Integer searchAll(LocalDateTime localBeginTime, LocalDateTime localEndTime);

    List<GoodsSalesDTO> searchDetailsTop10(LocalDateTime localBeginTime, LocalDateTime localEndTime);
}

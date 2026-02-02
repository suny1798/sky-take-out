package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeOutOrder(){
        log.info("处理超时订单, {}", LocalDateTime.now());

        List<Orders> orders = orderMapper.processTimeOutOrder(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));
        if (orders != null && !orders.isEmpty()){
            for(Orders ordersDB : orders){
                ordersDB.setStatus(Orders.CANCELLED);
                ordersDB.setCancelReason("支付超时,自动取消");
                ordersDB.setCancelTime(LocalDateTime.now());
                orderMapper.update(ordersDB);
            }
        }

    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processCancelOrder(){
        log.info("处理一直派单订单");
        List<Orders> orders = orderMapper.processCancelOrder(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusHours(1));
        if (orders != null && !orders.isEmpty()){
            for(Orders ordersDB : orders){
                ordersDB.setStatus(Orders.COMPLETED);
                orderMapper.update(ordersDB);
            }
        }

    }
}

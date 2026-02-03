package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 获取营业额统计报表
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return 营业额统计报表
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> datelist = new ArrayList<>();
        datelist.add(begin);
        while (begin.isBefore(end)) {
            begin = begin.plusDays(1);
            datelist.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate localDate : datelist) {
            //查询已完成订单的一天的营业额
            LocalDateTime localBeginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime LocalEndTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Double turnover =  orderMapper.searchAmountByDate(localBeginTime, LocalEndTime, Orders.COMPLETED);
            if (turnover == null) {
                turnover = 0.0;
            }
            turnoverList.add(turnover);
        }

        String joinDate = StringUtils.join(datelist, ",");
        return TurnoverReportVO.builder()
                .dateList(joinDate)
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }


    /**
     * 获取用户统计报表
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return 用户统计报表
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> datelist = new ArrayList<>();
        datelist.add(begin);
        while (begin.isBefore(end)) {
            begin = begin.plusDays(1);
            datelist.add(begin);
        }

        List<Integer> userList = new ArrayList<>();
        List<Integer> NewuserList = new ArrayList<>();

        for (LocalDate localDate : datelist) {
            LocalDateTime localBeginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime LocalEndTime = LocalDateTime.of(localDate, LocalTime.MAX);
            //查询当天的所有用户
            Integer totaluser = userMapper.searchUserByEndTime(LocalEndTime);
            //查询当天新增的用户
            Integer newuser = userMapper.searchUserBycreateTime(localBeginTime, LocalEndTime);
            userList.add(totaluser);
            NewuserList.add(newuser);
        }

        return new UserReportVO().builder()
                .dateList(StringUtils.join(datelist, ","))
                .totalUserList(StringUtils.join(userList, ","))
                .newUserList(StringUtils.join(NewuserList, ","))
                .build();
    }

    /**
     * 获取订单统计报表
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return 订单统计报表
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> datelist = new ArrayList<>();
        datelist.add(begin);
        while (begin.isBefore(end)) {
            begin = begin.plusDays(1);
            datelist.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;
        for (LocalDate localDate : datelist) {
            LocalDateTime localBeginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime LocalEndTime = LocalDateTime.of(localDate, LocalTime.MAX);
            //查询当天的所有订单
            Integer totalorder = orderMapper.searchAll(localBeginTime, LocalEndTime);
            orderCountList.add(totalorder);
            //查询当天的所有已完成订单
            Integer completedorder = orderMapper.searchByDate(localBeginTime, LocalEndTime, Orders.COMPLETED);
            validOrderCountList.add(completedorder);
            totalOrderCount += totalorder;
            validOrderCount += completedorder;
        }

        return new OrderReportVO().builder()
                .dateList(StringUtils.join(datelist, ","))
                .orderCompletionRate(((double)validOrderCount / totalOrderCount))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .build();
    }

    /**
     * 获取销售前十统计报表
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return 销售前十统计报表
     */
    public SalesTop10ReportVO getSalesTop10Report(LocalDate begin, LocalDate end) {

        LocalDateTime localBeginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime LocalEndTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTO = orderMapper.searchDetailsTop10(localBeginTime, LocalEndTime);

        List<String> collect = goodsSalesDTO.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = goodsSalesDTO.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(collect, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }
}

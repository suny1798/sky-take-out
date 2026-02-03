package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

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
            Double turnover =  orderMapper.searchByDate(localBeginTime, LocalEndTime, Orders.COMPLETED);
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
}

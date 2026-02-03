package com.sky.controller.admin;

import com.aliyuncs.http.HttpResponse;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController("adminReportController")
@RequestMapping("/admin/report")
@Slf4j
@Api(tags = "数据报表管理")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 营业额统计
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return 营业额统计结果
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> turnoverReport(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {

        log.info("营业额统计，begin：{}，end：{}", begin, end);
        TurnoverReportVO turnoverStatistics = reportService.getTurnoverStatistics(begin, end);
        return Result.success(turnoverStatistics);
    }

    /**
     * 用户统计报表
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return 用户统计报表结果
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户统计报表")
    public Result<UserReportVO> userReport(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {

        log.info("用户统计报表，begin：{}，end：{}", begin, end);
        UserReportVO userReportVO = reportService.getUserStatistics(begin, end);
        return Result.success(userReportVO);
    }

    /**
     * 订单统计报表
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return 订单统计报表结果
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计报表")
    public Result<OrderReportVO> orderStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {

        log.info("订单统计报表，begin：{}，end：{}", begin, end);
        OrderReportVO orderReportVO = reportService.getOrderStatistics(begin, end);
        return Result.success(orderReportVO);
    }

    /**
     * 销售前十统计报表
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return 销售前十统计报表结果
     */
    @GetMapping("/top10")
    @ApiOperation("销售前十统计报表")
    public Result<SalesTop10ReportVO> top10ReportVOResult(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("销售前十统计报表，begin：{}，end：{}", begin, end);
        SalesTop10ReportVO salesTop10ReportVO = reportService.getSalesTop10Report(begin, end);
        return Result.success(salesTop10ReportVO);
    }

    /**
     * 导出Excel
     * @return
     */
    @GetMapping("/export")
    @ApiOperation("导出Excel")
    public void exportExcel(HttpServletResponse response){
        log.info("导出Excel");
        reportService.exportBusinessData(response);
    }
}

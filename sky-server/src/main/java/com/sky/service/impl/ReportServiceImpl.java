package com.sky.service.impl;

import com.aliyuncs.http.HttpResponse;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService workspaceService;

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


    /**
     * 导出运营数据报表
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

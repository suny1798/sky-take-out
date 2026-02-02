package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyTask {

//    @Scheduled(cron = "0/5 * * * * ? ")
    public void task1(){
        log.info("开始执行定时任务1 " + LocalDateTime.now());
    }
}

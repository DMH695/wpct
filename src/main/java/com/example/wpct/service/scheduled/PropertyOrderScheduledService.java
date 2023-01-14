package com.example.wpct.service.scheduled;

import com.example.wpct.service.impl.PropertyOrderServiceImpl;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

@Service
@Slf4j
public class PropertyOrderScheduledService {


    @Autowired
    @Lazy
    private PropertyOrderServiceImpl propertyOrderService;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 每月一号晚上两点执行生成物业费计划任务
     */
    @Scheduled(cron = "0 0 0,2 1 * ?")
    public void generateOrders(){
        log.info("生成物业订单数：" + propertyOrderService.generateOrders());
    }

    /**
     * 每月一号晚上三点执行自动缴交物业费计划任务
     */
    @Scheduled(cron = "0 0 0,3 1 * ?")
    public void automaticPayment(){
        log.info("物业费自动缴交订单成功数量：" + propertyOrderService.automaticPayment());
    }
}

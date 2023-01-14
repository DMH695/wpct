package com.example.wpct.service.scheduled;

import com.example.wpct.service.impl.SharedFeeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SharedFeeScheduledService {

    @Autowired
    @Lazy
    private SharedFeeServiceImpl sharedFeeService;

    /**
     * 每月二号晚上三点执行自动缴交物业费计划任务
     */
    @Scheduled(cron = "0 0 0,3 2 * ?")
    public void automaticPayment(){
        log.info("公摊费自动缴交订单成功数量：" + sharedFeeService.automaticPayment());
    }
}

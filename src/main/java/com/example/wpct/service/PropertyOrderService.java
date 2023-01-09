package com.example.wpct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wpct.entity.PropertyOrderDto;

public interface PropertyOrderService extends IService<PropertyOrderDto> {

    int generateOrders();

    int automaticPayment();

}

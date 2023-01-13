package com.example.wpct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wpct.entity.PropertyOrderDto;
import com.example.wpct.entity.vo.PropertyOrderVo;
import com.example.wpct.utils.ResultBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface PropertyOrderService extends IService<PropertyOrderDto> {

    int generateOrders();

    int automaticPayment();

    ResultBody list(PropertyOrderVo vo);

    void getTemplate(HttpServletResponse response);

    ResultBody importPropertyOrder(MultipartFile file);

    ResultBody listByUser(String openid);

}

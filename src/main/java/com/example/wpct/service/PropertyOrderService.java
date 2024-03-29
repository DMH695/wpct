package com.example.wpct.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.PropertyOrderDto;
import com.example.wpct.entity.SharedFeeOrderDto;
import com.example.wpct.entity.vo.PropertyOrderVo;
import com.example.wpct.utils.ResultBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface PropertyOrderService extends IService<PropertyOrderDto> {

    int generateOrders();

    int automaticPayment();

    ResultBody list(PropertyOrderVo vo);

    ResultBody listByHouseId(long houseId);

    void getTemplate(HttpServletResponse response);

    ResultBody importPropertyOrder(MultipartFile file);

    ResultBody listByUser(String openid);

    ResultBody insert(PropertyOrderDto dto);

    double calcCost(HousingInformationDto dto);

    double houseCount(long hid);

    JSONObject getCostDetail(HousingInformationDto dto);


    void updateRid(long id,Integer rid);

    PropertyOrderDto selectById(Long id);
}

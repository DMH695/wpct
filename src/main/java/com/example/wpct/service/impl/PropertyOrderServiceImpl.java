package com.example.wpct.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.PropertyOrderDto;
import com.example.wpct.mapper.PropertyOrderMapper;
import com.example.wpct.service.PropertyOrderService;
import com.example.wpct.utils.SnowFlakeIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
@Slf4j
public class PropertyOrderServiceImpl extends ServiceImpl<PropertyOrderMapper, PropertyOrderDto> implements PropertyOrderService {

    @Autowired
    private HousingInformationServiceImpl housingInformationService;

    /**
     * 生成物业费订单
     * @return 生成的订单数量
     */
    @Override
    public int generateOrders() {
        List<HousingInformationDto> housingInformationDtoList = housingInformationService.query().list();
        List<PropertyOrderDto> propertyOrderDtoList = new ArrayList<>();
        SnowFlakeIdUtils idUtils = new SnowFlakeIdUtils(3, 1);
        for (HousingInformationDto dto : housingInformationDtoList) {
            double cost = dto.getArea() * dto.getAreaUnitPrice() + dto.getExceedArea() * dto.getExceedAreaUnitPrice() +
                    dto.getCarFee() + dto.getOtherFee() + dto.getRecycleFee() + dto.getRecycleRent() + dto.getCalculateRent() +
                    dto.getCalculateFee() + dto.getDiscount();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new java.util.Date());
            calendar.add(Calendar.MONTH,1);
            PropertyOrderDto buildResult = PropertyOrderDto.builder()
                    .orderNo(idUtils.nextId()).houseId(dto.getId()).paymentStatus(0).cost(cost)
                    .costDetail(getCostDetail(dto).toJSONString()).beginDate(new Date(System.currentTimeMillis()))
                    .endDate(new Date(calendar.getTimeInMillis())).updateTime(new Timestamp(System.currentTimeMillis())).build();
            propertyOrderDtoList.add(buildResult);
            log.info("生成订单：{}",buildResult);
        }
        saveBatch(propertyOrderDtoList);
        return propertyOrderDtoList.size();
    }

    /**
     * 自动缴交物业费
     * @return 缴交成功数量
     */
    @Override
    public int automaticPayment() {
        return 0;
    }


    /**
     * 获取房屋的物业费详细json
     * @param dto 房屋信息
     * @return json
     */
    private JSONObject getCostDetail(HousingInformationDto dto) {
        JSONObject res = new JSONObject();
        res.put("基础面积费用", dto.getArea() * dto.getAreaUnitPrice());
        res.put("超出面积费用", dto.getExceedArea() * dto.getExceedAreaUnitPrice());
        res.put("停车费", dto.getCarFee());
        res.put("其他费用", dto.getOtherFee());
        res.put("收回不符条件疫情减免金额", dto.getRecycleFee());
        res.put("收回不符合条件租金", dto.getRecycleRent());
        res.put("应收应退租金",dto.getCalculateRent());
        res.put("应收应退物业费",dto.getCalculateFee());
        res.put("优惠",dto.getDiscount());
        return res;
    }
}

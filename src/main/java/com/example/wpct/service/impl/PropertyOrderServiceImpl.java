package com.example.wpct.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.PropertyOrderDto;
import com.example.wpct.mapper.PropertyOrderMapper;
import com.example.wpct.service.PropertyOrderService;
import com.example.wpct.utils.SnowFlakeIdUtils;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     *
     * @return 生成的订单数量
     */
    @Override
    @Transactional
    @Synchronized
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
            calendar.add(Calendar.MONTH, 1);
            PropertyOrderDto buildResult = PropertyOrderDto.builder()
                    .orderNo(idUtils.nextId()).houseId(dto.getId()).paymentStatus(0).cost(cost)
                    .costDetail(getCostDetail(dto).toJSONString()).beginDate(new Date(System.currentTimeMillis()))
                    .endDate(new Date(calendar.getTimeInMillis())).updateTime(new Timestamp(System.currentTimeMillis())).build();
            propertyOrderDtoList.add(buildResult);
            log.info("生成订单：{}", buildResult);
        }
        saveBatch(propertyOrderDtoList);
        return propertyOrderDtoList.size();
    }

    /**
     * 自动缴交物业费
     *
     * @return 缴交成功数量
     */
    @Override
    @Transactional
    @Synchronized
    public int automaticPayment() {
        List<PropertyOrderDto> notPayment = query().eq("payment_status", 0).list();
        List<HousingInformationDto> afterUpdateHouseList = new ArrayList<>();
        List<PropertyOrderDto> afterUpdateOrderList = new ArrayList<>();
        for (PropertyOrderDto dto : notPayment) {
            HousingInformationDto house = housingInformationService.query().eq("id", dto.getHouseId()).one();
            if (house.getPropertyFee() >= dto.getCost()) {
                log.info("{}#{}#{}房屋自动缴费成功，缴交{}元，剩余{}元"
                        , house.getVillageName(), house.getBuildNumber(), house.getHouseNo()
                        , dto.getCost(), house.getPropertyFee() - dto.getCost()
                );
                house.setPropertyFee(house.getPropertyFee() - dto.getCost());
                house.setDueDate(new Timestamp(System.currentTimeMillis()));
                house.setUpdated(new Timestamp(System.currentTimeMillis()));
                afterUpdateHouseList.add(house);
                dto.setPaymentStatus(1);
                dto.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                afterUpdateOrderList.add(dto);
            } else {
                log.info("{}#{}#{}房屋自动缴费失败,余额不足"
                        , house.getVillageName(), house.getBuildNumber(), house.getHouseNo()
                );
            }
        }
        housingInformationService.updateBatchById(afterUpdateHouseList);
        this.updateBatchById(afterUpdateOrderList);
        return afterUpdateHouseList.size();
    }


    /**
     * 获取房屋的物业费详细json
     *
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
        res.put("应收应退租金", dto.getCalculateRent());
        res.put("应收应退物业费", dto.getCalculateFee());
        res.put("优惠", dto.getDiscount());
        return res;
    }
}

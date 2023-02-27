package com.example.wpct.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.PropertyOrderDto;
import com.example.wpct.entity.WechatUser;
import com.example.wpct.entity.vo.PropertyOrderVo;
import com.example.wpct.mapper.PropertyOrderMapper;
import com.example.wpct.mapper.WechatUserMapper;
import com.example.wpct.service.PropertyOrderService;
import com.example.wpct.utils.ResultBody;
import com.example.wpct.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
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

    @Autowired
    private WechatUserMapper wechatUserMapper;



    /**
     * 生成物业费订单
     *
     * @return 生成的订单数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Synchronized
    public int generateOrders() {
        List<HousingInformationDto> housingInformationDtoList = housingInformationService.query().list();
        List<PropertyOrderDto> propertyOrderDtoList = new ArrayList<>();
        Snowflake snowflake = new Snowflake();
        for (HousingInformationDto dto : housingInformationDtoList) {
            double cost = calcCost(dto);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new java.util.Date());
            calendar.add(Calendar.MONTH, 1);
            PropertyOrderDto buildResult = PropertyOrderDto.builder()
                    .orderNo(snowflake.nextId()).houseId(dto.getId()).paymentStatus(0).cost(cost)
                    .costDetail(getCostDetail(dto).toJSONString()).beginDate(new Date(System.currentTimeMillis()))
                    .endDate(new Date(calendar.getTimeInMillis())).updateTime(new Timestamp(System.currentTimeMillis()).toString()).build();
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
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Synchronized
    public int automaticPayment() {
        List<PropertyOrderDto> notPayment = query().eq("payment_status", 0).list();
        List<PropertyOrderDto> afterUpdateOrderList = new ArrayList<>();
        for (PropertyOrderDto dto : notPayment) {
            HousingInformationDto house = housingInformationService.query().eq("id", dto.getHouseId()).one();
            if (house == null){
                log.info("房屋id"+dto.getHouseId()+"不存在,缴交失败");
                continue;
            }
            if (house.getPropertyFee() >= dto.getCost()) {
                log.info("{}#{}#{}房屋自动缴费成功，缴交{}元，剩余{}元"
                        , house.getVillageName(), house.getBuildNumber(), house.getHouseNo()
                        , dto.getCost(), house.getPropertyFee() - dto.getCost()
                );
                house.setPropertyFee(house.getPropertyFee() - dto.getCost());
                house.setDueDate(new Timestamp(System.currentTimeMillis()).toString());
                house.setUpdated(new Timestamp(System.currentTimeMillis()).toString());
                housingInformationService.updateById(house);
                dto.setPaymentStatus(1);
                dto.setUpdateTime(new Timestamp(System.currentTimeMillis()).toString());
                afterUpdateOrderList.add(dto);
            } else {
                log.info("{}#{}#{}房屋自动缴费失败,余额不足"
                        , house.getVillageName(), house.getBuildNumber(), house.getHouseNo()
                );
            }
        }
        this.updateBatchById(afterUpdateOrderList);
        return afterUpdateOrderList.size();
    }

    @Override
    public ResultBody list(PropertyOrderVo vo) {
        List<Long> houseIds = housingInformationService.getIdsByHouseInfo(
                vo.getVillageName(), vo.getBuildNumber(), vo.getHouseNo()
        );
        PageHelper.startPage(vo.getPageNum(),vo.getPageSize());
        List<PropertyOrderDto> orderList = this.query()
                .in("house_id", houseIds)
                .le(StringUtils.isNotEmpty(vo.getEndDate()), "end_date", vo.getEndDate())
                .ge(StringUtils.isNotEmpty(vo.getBeginDate()), "begin_date", vo.getBeginDate())
                .eq(vo.getPaymentStatus() != null,"payment_status",vo.getPaymentStatus())
                .list();
        for (PropertyOrderDto propertyOrderDto : orderList) {
            HousingInformationDto house = housingInformationService.query().eq("id", propertyOrderDto.getHouseId()).one();
            propertyOrderDto.setVillageName(house.getVillageName());
            propertyOrderDto.setBuildNumber(house.getBuildNumber());
            propertyOrderDto.setHouseNo(house.getHouseNo());
        }
        PageInfo<PropertyOrderDto> pageInfo = new PageInfo<>(orderList,vo.getPageSize());
        return ResultBody.ok(pageInfo);
    }

    @Override
    public ResultBody listByHouseId(long houseId) {
        List<PropertyOrderDto> orders = query().eq("house_id", houseId).list();
        for (PropertyOrderDto order : orders) {
            HousingInformationDto house = housingInformationService.query().eq("id", order.getHouseId()).one();
            order.setVillageName(house.getVillageName());
            order.setBuildNumber(house.getBuildNumber());
            order.setHouseNo(house.getHouseNo());
        }
        return ResultBody.ok(orders);
    }

    @Override
    public void getTemplate(HttpServletResponse response) {

    }

    @Override
    public ResultBody importPropertyOrder(MultipartFile file) {
        return null;
    }

    @Override
    public ResultBody listByUser(String openid) {
        QueryWrapper<WechatUser> query = new QueryWrapper<>();
        query.eq("openid",openid);
        List<WechatUser> wechatUsers = wechatUserMapper.selectList(query);
        JSONArray res = new JSONArray();
        for (WechatUser wechatUser : wechatUsers) {
            JSONObject tmp = new JSONObject();
            HousingInformationDto house = housingInformationService.query().eq("id", wechatUser.getHid()).one();
            if (house == null)
                continue;
            tmp.put("house",String.format("%s#%s#%s",house.getVillageName(),house.getBuildNumber(),house.getHouseNo()));
            tmp.put("property_order",query().eq("house_id",house.getId()).list());
            res.add(tmp);
        }
        return ResultBody.ok(res);
    }

    @Override
    public ResultBody insert(PropertyOrderDto dto) {
        Snowflake snowflake = new Snowflake();
        dto.setUpdateTime(new Timestamp(System.currentTimeMillis()).toString());
        dto.setOrderNo(snowflake.nextId());
        dto.setEndDate(new Date(System.currentTimeMillis()));
        dto.setBeginDate(new Date(System.currentTimeMillis() - 3600L * 1000L * 24L * 30L));
        return ResultBody.ok(save(dto));
    }

    @Override
    public double calcCost(HousingInformationDto dto) {
        return dto.getArea() * dto.getAreaUnitPrice() + dto.getExceedArea() * dto.getExceedAreaUnitPrice() +
                dto.getCarFee() + dto.getOtherFee() + dto.getRecycleFee() + dto.getRecycleRent() + dto.getCalculateRent() +
                dto.getCalculateFee() + dto.getDiscount();
    }

    @Override
    public double houseCount(long hid) {
        List<PropertyOrderDto> propertyOrders = this.query().eq("house_id", hid).list();
        double count = 0;
        for (PropertyOrderDto propertyOrder : propertyOrders) {
            count+= propertyOrder.getCost();
        }
        return count;
    }


    /**
     * 获取房屋的物业费详细json
     *
     * @param dto 房屋信息
     * @return json
     */
    public JSONObject getCostDetail(HousingInformationDto dto) {
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

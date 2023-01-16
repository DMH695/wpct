package com.example.wpct.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.SharedFeeOrderDto;
import com.example.wpct.entity.model.SharedFeeOrderImportModel;
import com.example.wpct.entity.vo.SharedFeeOrderVo;
import com.example.wpct.mapper.SharedFeeOrderMapper;
import com.example.wpct.service.SharedFeeOrderService;
import com.example.wpct.utils.ResultBody;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SharedFeeOrderServiceImpl extends ServiceImpl<SharedFeeOrderMapper, SharedFeeOrderDto> implements SharedFeeOrderService {

    @Autowired
    private HousingInformationServiceImpl housingInformationService;

    @Override
    public ResultBody insert(SharedFeeOrderDto dto) {
        Snowflake snowflake = new Snowflake();
        dto.setUpdateDate(new Timestamp(System.currentTimeMillis()).toString());
        dto.setOrderNo(snowflake.nextId());
        return ResultBody.ok(save(dto));
    }

    @Override
    public ResultBody list(SharedFeeOrderVo vo) {
        List<Long> houseIds = housingInformationService.getIdsByHouseInfo(
                vo.getVillageName(), vo.getBuildNumber(), vo.getHouseNo()
        );
        PageHelper.startPage(vo.getPageNum(), vo.getPageSize());
        List<SharedFeeOrderDto> orderList = this.query()
                .in("house_id", houseIds)
                .eq(vo.getPaymentStatus() != null, "payment_status", vo.getPaymentStatus())
                .le(vo.getPaymentStatus() != null && vo.getPay_time_end() != null, "pay_time", vo.getPay_time_end())
                .ge(vo.getPaymentStatus() != null && vo.getPay_time_begin() != null, "pay_time", vo.getPay_time_begin())
                .list();
        PageInfo<SharedFeeOrderDto> pageInfo = new PageInfo<>(orderList, vo.getPageSize());
        return ResultBody.ok(pageInfo);
    }

    @SneakyThrows
    @Override
    public void getTemplate(HttpServletResponse response) {
        Snowflake snowflake = new Snowflake();
        List<SharedFeeOrderImportModel> excelList = new ArrayList<>();
        SharedFeeOrderImportModel order = SharedFeeOrderImportModel.builder()
                .villageName("某某小区").buildNumber("一单元").houseNo("301")
                .liftFee(12).eleFee(123).waterFee(12).paymentStatus(true)
                .payTime("2023-01-15 06:16").endDate(new Date(System.currentTimeMillis()).toString())
                .beginDate(new Date(System.currentTimeMillis() + 10000).toString()).build();
        excelList.add(order);
        response.setHeader("Content-Disposition", "attachment;filename=" + snowflake.nextIdStr() + "template.xlsx");
        EasyExcel.write(response.getOutputStream())
                .head(SharedFeeOrderImportModel.class)
                .sheet("importTemplate")
                .doWrite(excelList);

    }

    @SneakyThrows
    @Override
    public ResultBody importOrder(MultipartFile file) {
        List<SharedFeeOrderImportModel> sharedFeeOrderImportModels = EasyExcel
                .read(file.getInputStream()).head(SharedFeeOrderImportModel.class).sheet().doReadSync();
        List<SharedFeeOrderDto> sharedFeeOrderList = new ArrayList<>();
        Snowflake snowflake = new Snowflake();
        for (SharedFeeOrderImportModel sharedFeeOrderImportModel : sharedFeeOrderImportModels) {
            HousingInformationDto house = housingInformationService.query()
                    .eq("village_name", sharedFeeOrderImportModel.getVillageName())
                    .eq("build_number", sharedFeeOrderImportModel.getBuildNumber())
                    .eq("house_no", sharedFeeOrderImportModel.getHouseNo()).one();
            long house_id;
            if (house == null)
                continue;
            else
                house_id = house.getId();
            double allFee = sharedFeeOrderImportModel.getEleFee()
                    + sharedFeeOrderImportModel.getLiftFee()
                    + sharedFeeOrderImportModel.getWaterFee();
            JSONObject detail = new JSONObject();
            detail.put("lift",sharedFeeOrderImportModel.getLiftFee());
            detail.put("ele",sharedFeeOrderImportModel.getEleFee());
            detail.put("water",sharedFeeOrderImportModel.getWaterFee());
            SharedFeeOrderDto sharedFeeOrderDto = SharedFeeOrderDto.builder()
                    .orderNo(snowflake.nextId()).houseId(house_id).cost(allFee)
                    .payTime(sharedFeeOrderImportModel.getPayTime())
                    .costDetail(detail.toJSONString()).beginDate(sharedFeeOrderImportModel.getBeginDate())
                    .endDate(sharedFeeOrderImportModel.getEndDate()).updateDate(new Date(System.currentTimeMillis()).toString())
                    .updateUser("admin").build();
            sharedFeeOrderList.add(sharedFeeOrderDto);
        }
        return ResultBody.ok(saveBatch(sharedFeeOrderList));
    }

    @Override
    @Transactional
    public ResultBody executeDeduction() {
        List<SharedFeeOrderDto> notPayments = query().eq("payment_status",0).list();
        List<SharedFeeOrderDto> updateOrderList = new ArrayList<>();
        for (SharedFeeOrderDto notPayment : notPayments) {
            HousingInformationDto house = housingInformationService.query().eq("id",notPayment.getHouseId()).one();
            if (house == null){
                log.info("房屋id"+notPayment.getHouseId()+"不存在,缴交失败");
                continue;
            }
            if (house.getPoolBalance() >= notPayment.getCost()){
                log.info("{}#{}#{}房屋缴费成功，缴交{}元，剩余{}元"
                        , house.getVillageName(), house.getBuildNumber(), house.getHouseNo()
                        , notPayment.getCost(), house.getPoolBalance() - notPayment.getCost()
                );
                house.setPropertyFee(house.getPoolBalance() - notPayment.getCost());
                house.setUpdated(new Timestamp(System.currentTimeMillis()).toString());
                housingInformationService.updateById(house);
                notPayment.setPaymentStatus(1);
                notPayment.setUpdateDate(new Timestamp(System.currentTimeMillis()).toString());
                updateOrderList.add(notPayment);
            }else {
                log.info("{}#{}#{}房屋缴费失败,余额不足"
                        , house.getVillageName(), house.getBuildNumber(), house.getHouseNo()
                );
            }
            this.updateBatchById(updateOrderList);
        }
        return ResultBody.ok(updateOrderList.size());
    }
}

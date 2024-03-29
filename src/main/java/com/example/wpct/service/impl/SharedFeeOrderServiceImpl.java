package com.example.wpct.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.SharedFeeOrderDto;
import com.example.wpct.entity.VillageDto;
import com.example.wpct.entity.model.SharedFeeOrderImportModel;
import com.example.wpct.entity.vo.SharedFeeOrderVo;
import com.example.wpct.mapper.SharedFeeOrderMapper;
import com.example.wpct.service.SharedFeeOrderService;
import com.example.wpct.utils.ResultBody;
import com.example.wpct.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    @Autowired
    @Lazy
    private VillageServiceImpl villageService;

    @Autowired
    SharedFeeOrderMapper sharedFeeOrderMapper;

    @Override
    public ResultBody insert(SharedFeeOrderDto dto) {
        Snowflake snowflake = new Snowflake();
        dto.setUpdateDate(new Timestamp(System.currentTimeMillis()).toString());
        dto.setOrderNo(snowflake.nextId());
        dto.setEndDate(new Date(System.currentTimeMillis()).toString());
        dto.setBeginDate(new Date(System.currentTimeMillis() - 3600L * 1000L * 24L * 30L).toString());
        return ResultBody.ok(save(dto));
    }

    @Override
    public ResultBody list(SharedFeeOrderVo vo) {
        List<Long> houseIds = housingInformationService.getIdsByHouseInfo(
                vo.getVillageName(), vo.getBuildNumber(), vo.getHouseNo()
        );
        PageHelper.startPage(vo.getPageNum(), vo.getPageSize());
        List<SharedFeeOrderDto> orderList = new ArrayList<>();
        if (vo.getCheck() == null || "".equals(vo.getCheck())){
             orderList = this.query()
                    .in("house_id", houseIds)
                    .eq(vo.getPaymentStatus() != null, "payment_status", vo.getPaymentStatus())
                    .le(StringUtils.isNotEmpty(vo.getPay_time_end()), "end_date", vo.getPay_time_end())
                    .ge(StringUtils.isNotEmpty(vo.getPay_time_begin()), "begin_date", vo.getPay_time_begin())
                    .list();
        }else if ("已审批".equals(vo.getCheck())){
            orderList = this.query()
                    .in("house_id", houseIds)
                    .isNull("rid")
                    .eq(vo.getPaymentStatus() != null, "payment_status", vo.getPaymentStatus())
                    .le(StringUtils.isNotEmpty(vo.getPay_time_end()), "end_date", vo.getPay_time_end())
                    .ge(StringUtils.isNotEmpty(vo.getPay_time_begin()), "begin_date", vo.getPay_time_begin())
                    .list();
        }else if("待审批".equals(vo.getCheck())){
            orderList = this.query()
                    .in("house_id", houseIds)
                    .isNotNull("rid")
                    .eq(vo.getPaymentStatus() != null, "payment_status", vo.getPaymentStatus())
                    .le(StringUtils.isNotEmpty(vo.getPay_time_end()), "end_date", vo.getPay_time_end())
                    .ge(StringUtils.isNotEmpty(vo.getPay_time_begin()), "begin_date", vo.getPay_time_begin())
                    .list();
        }

        for (SharedFeeOrderDto order : orderList) {
            HousingInformationDto house = housingInformationService.query().eq("id", order.getHouseId()).one();
            order.setVillageName(house.getVillageName());
            order.setBuildNumber(house.getBuildNumber());
            order.setHouseNo(house.getHouseNo());
        }
        PageInfo<SharedFeeOrderDto> pageInfo = new PageInfo<>(orderList, vo.getPageSize());
        return ResultBody.ok(pageInfo);
    }

    @SneakyThrows
    @Override
    public void getTemplate(HttpServletResponse response) {
        Snowflake snowflake = new Snowflake();
        List<SharedFeeOrderImportModel> excelList = new ArrayList<>();
        List<VillageDto> villageList = villageService.query().list();
        for (VillageDto village : villageList) {
            excelList.add(
                    SharedFeeOrderImportModel.builder()
                            .villageName(village.getName())
                            .beginDate(new Date(System.currentTimeMillis()).toString())
                            .build());
        }
        ;
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
            List<HousingInformationDto> houseList = housingInformationService
                    .listByVillageName(sharedFeeOrderImportModel.getVillageName());
            for (HousingInformationDto house : houseList) {
                double cost = sharedFeeOrderImportModel.getWaterFee()
                        + sharedFeeOrderImportModel.getLiftFee()
                        + sharedFeeOrderImportModel.getEleFee();
                JSONObject costDetail = new JSONObject();
                costDetail.put("公摊电梯费", sharedFeeOrderImportModel.getLiftFee());
                costDetail.put("公摊水费", sharedFeeOrderImportModel.getWaterFee());
                costDetail.put("公摊电费", sharedFeeOrderImportModel.getEleFee());
                if (sharedFeeOrderImportModel.getBeginDate() == null || sharedFeeOrderImportModel.getEndDate() == null)
                    continue;
                SharedFeeOrderDto order = SharedFeeOrderDto.builder()
                        .orderNo(snowflake.nextId()).houseId(house.getId()).cost(cost).paymentStatus(0)
                        .costDetail(costDetail.toJSONString()).beginDate(sharedFeeOrderImportModel.getBeginDate())
                        .endDate(sharedFeeOrderImportModel.getEndDate()).updateDate(new Timestamp(System.currentTimeMillis()).toString())
                        .updateUser("admin").build();
                sharedFeeOrderList.add(order);
            }

        }
        return ResultBody.ok(saveBatch(sharedFeeOrderList));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public ResultBody executeDeduction() {
        List<SharedFeeOrderDto> notPayments = query().eq("payment_status", 0).list();
        List<SharedFeeOrderDto> updateOrderList = new ArrayList<>();
        for (SharedFeeOrderDto notPayment : notPayments) {
            HousingInformationDto house = housingInformationService.query().eq("id", notPayment.getHouseId()).one();
            if (house == null) {
                log.info("房屋id" + notPayment.getHouseId() + "不存在,缴交失败");
                continue;
            }
            if (house.getPoolBalance() >= notPayment.getCost()) {
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
            } else {
                log.info("{}#{}#{}房屋缴费失败,余额不足"
                        , house.getVillageName(), house.getBuildNumber(), house.getHouseNo()
                );
            }
            this.updateBatchById(updateOrderList);
        }
        return ResultBody.ok(updateOrderList.size());
    }

    @Override
    public ResultBody updateByDto(SharedFeeOrderDto dto) {
        dto.setUpdateDate(new Date(System.currentTimeMillis()).toString());
        return ResultBody.ok(updateById(dto));
    }

    @Override
    public double houseCount(long hid) {
        List<SharedFeeOrderDto> sharedFeeOrders = this.query().eq("house_id", hid).list();
        double count = 0;
        for (SharedFeeOrderDto sharedFeeOrder : sharedFeeOrders) {
            if (sharedFeeOrder.getPaymentStatus() == 0){
                count+= sharedFeeOrder.getCost();
            }
        }
        return count;
    }

    @Override
    public void updateRid(long id, Integer rid) {
        sharedFeeOrderMapper.updateRid(id, rid);
    }

    @Override
    public SharedFeeOrderDto selectById(Long id) {
        return sharedFeeOrderMapper.getById(id);
    }
}

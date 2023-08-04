package com.example.wpct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.wpct.entity.*;
import com.example.wpct.service.ReportService;
import com.example.wpct.utils.DateThis;
import com.example.wpct.utils.DecimalUtils;
import com.example.wpct.utils.ResultBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    @Lazy
    private PropertyOrderServiceImpl propertyOrderService;

    @Autowired
    @Lazy
    private SharedFeeOrderServiceImpl sharedFeeOrderService;

    @Autowired
    @Lazy
    private HousingInformationServiceImpl housingInformationService;


    @Override
    public ResultBody getReport(String startDate, String endDate, String villageName) {
        List<ReportDto> reportList = new ArrayList<>();
        QueryWrapper<HousingInformationDto> houseQuery = new QueryWrapper<>();
        houseQuery.eq("village_name", villageName);
        List<HousingInformationDto> houses = housingInformationService.list(houseQuery);
        int[] end = Arrays.stream(endDate.split("-")).mapToInt(Integer::parseInt).toArray();
        DateThis dateThis = new DateThis();
        Calendar calendar = Calendar.getInstance();
        //noinspection MagicConstant
        calendar.set(end[0], end[1] - 1, end[2]);
        dateThis.setLocalTime(calendar);
        String deadline = dateThis.thisMonthEnd();
        for (HousingInformationDto house : houses) {
            List<PropertyOrderDto> propertyOrders = propertyOrderService.query().eq("house_id", house.getId())
                    .between("end_date", startDate, endDate).list();
            List<SharedFeeOrderDto> sharedFeeOrders = sharedFeeOrderService.query().eq("house_id", house.getId())
                    .between("end_date", startDate, endDate).list();
            double pReceivable = 0;
            double sReceivable = 0;
            double pReceived = 0;
            double sReceived = 0;
            int pCount = 0;
            int sCount = 0;
            double po, so;
            for (PropertyOrderDto dto : propertyOrders) {
                pReceivable += dto.getCost();
                pReceived += dto.getPaymentStatus() == 1 ? dto.getCost() : 0;
                if (dto.getPaymentStatus() == 0){
                    pCount = pCount + 1;
                }
            }
            for (SharedFeeOrderDto dto : sharedFeeOrders) {
                sReceivable += dto.getCost();
                sReceived += dto.getPaymentStatus() == 1 ? dto.getCost() : 0;
                if (dto.getPaymentStatus() == 0){
                    sCount = sCount + 1;
                }
            }
            double subP = DecimalUtils.subDouble(pReceivable, pReceived);
            double subS = DecimalUtils.subDouble(sReceivable, sReceived);
            po = pReceivable != pReceived && house.getPropertyFee() < subP ? DecimalUtils.subDouble(subP, house.getPropertyFee()) : 0;
            so = sReceivable != sReceived && house.getPoolBalance() < subS ? DecimalUtils.subDouble(subS, house.getPoolBalance()) : 0;
            reportList.add(
                    ReportDto.builder()
                            .pCount(pCount)
                            .sCount(sCount)
                            .buildNumber(house.getBuildNumber())
                            .houseName(house.getHouseNo())
                            .name(house.getName())
                            .phone(house.getPhone())
                            .propertyOutstanding(po)
                            .sharedOutstanding(so)
                            .receivable(pReceivable + sReceivable)
                            .received(pReceived + pReceived)
                            .deadline(deadline).build()
            );

        }
        return ResultBody.ok(reportList);
    }

}

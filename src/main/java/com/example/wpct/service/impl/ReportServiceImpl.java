package com.example.wpct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.wpct.entity.*;
import com.example.wpct.mapper.BuildMapper;
import com.example.wpct.mapper.VillageMapper;
import com.example.wpct.service.ReportService;
import com.example.wpct.utils.ResultBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private VillageMapper villageMapper;

    @Autowired
    private BuildMapper buildMapper;

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
    public ResultBody getReport() {
        List<ReportDto> reportList = new ArrayList<>();
        List<VillageDto> villages = villageMapper.selectList(null);
        for (VillageDto village : villages) {
            QueryWrapper<BuildDto> buildQuery = new QueryWrapper<>();
            buildQuery.eq("village_id", village.getId());
            List<BuildDto> builds = buildMapper.selectList(buildQuery);
            for (BuildDto build : builds) {
                List<Long> houseIds = housingInformationService.getIdsByHouseInfo(village.getName(), build.getName(), null);
                // 天 周 月 毫秒数
                long[] lessMs = new long[]{86400000L, 604800000L, 2592000000L};
                for (long lm : lessMs) {
                    List<PropertyOrderDto> property = propertyOrderService.query()
                            .in(houseIds.size() > 0, "house_id", houseIds)
                            .between("end_date", new Date(System.currentTimeMillis() - lm), new Date(System.currentTimeMillis())).list();
                    List<SharedFeeOrderDto> sharedFee = sharedFeeOrderService.query()
                            .in(houseIds.size() > 0, "house_id", houseIds)
                            .between("end_date", new Date(System.currentTimeMillis() - lm), new Date(System.currentTimeMillis())).list();
                    double receivable = 0;
                    double received = 0;
                    float payed = 0;
                    for (PropertyOrderDto dto : property) {
                        receivable += dto.getCost();
                        received += dto.getPaymentStatus() == 1 ? dto.getCost() : 0;
                        payed += dto.getPaymentStatus() == 1 ? 1 : 0;
                    }
                    for (SharedFeeOrderDto dto : sharedFee) {
                        receivable += dto.getCost();
                        received += dto.getPaymentStatus() == 1 ? dto.getCost() : 0;
                        payed += dto.getPaymentStatus() == 1 ? 1 : 0;
                    }
                    reportList.add(
                            ReportDto.builder()
                                    .villageName(village.getName()).buildNumber(build.getName())
                                    .startDate(new Date(System.currentTimeMillis() - lm))
                                    .endDate(new Date(System.currentTimeMillis()))
                                    .receivable(receivable).received(received)
                                    .rate(payed / (float) (property.size() + sharedFee.size()))
                                    .build()
                    );
                }
            }
        }
        return ResultBody.ok(reportList);
    }

}

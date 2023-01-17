package com.example.wpct.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.SharedFeeDto;
import com.example.wpct.entity.WechatUser;
import com.example.wpct.entity.model.SharedFeeImportModel;
import com.example.wpct.entity.vo.SharedFeeVo;
import com.example.wpct.mapper.SharedFeeMapper;
import com.example.wpct.mapper.WechatUserMapper;
import com.example.wpct.service.SharedFeeService;
import com.example.wpct.utils.ResultBody;
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
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SharedFeeServiceImpl extends ServiceImpl<SharedFeeMapper, SharedFeeDto> implements SharedFeeService {

    @Autowired
    @Lazy
    private HousingInformationServiceImpl housingInformationService;

    @Autowired
    private WechatUserMapper wechatUserMapper;

    @Override
    public ResultBody list(SharedFeeVo vo) {
        PageHelper.startPage(vo.getPageNum(), vo.getPageSize());
        String villageName = vo.getVillageName();
        String buildNumber = vo.getBuildNumber();
        String houseNo = vo.getHouseNo();
        List<Long> ids = housingInformationService.getIdsByHouseInfo(villageName, buildNumber, houseNo);
        return ResultBody.ok(new PageInfo<>(query().in("house_id", ids).list()));
    }

    @SneakyThrows
    @Override
    public void getTemplate(HttpServletResponse response) {
        Snowflake snowflake = new Snowflake();
        List<SharedFeeImportModel> excelList = new ArrayList<>();
        SharedFeeImportModel example = SharedFeeImportModel.builder()
                .villageName("某某某小区").buildNumber("一单元").houseNo("1203")
                .liftFee(123).eleFee(1234).waterFee(123).build();
        excelList.add(example);
        response.setHeader("Content-Disposition", "attachment;filename=" + snowflake.nextIdStr() + "template.xlsx");
        EasyExcel.write(response.getOutputStream())
                .head(SharedFeeImportModel.class)
                .sheet("importTemplate")
                .doWrite(excelList);
    }

    @SneakyThrows
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public ResultBody importSharedFee(MultipartFile file) {
        List<SharedFeeImportModel> sharedFeeImportModels = EasyExcel
                .read(file.getInputStream()).head(SharedFeeImportModel.class).sheet().doReadSync();
        List<SharedFeeDto> sharedFeeDtoList = new ArrayList<>();
        for (SharedFeeImportModel sharedFeeImportModel : sharedFeeImportModels) {
            HousingInformationDto one = housingInformationService.query()
                    .eq("village_name", sharedFeeImportModel.getVillageName())
                    .eq("build_number", sharedFeeImportModel.getBuildNumber())
                    .eq("house_no", sharedFeeImportModel.getHouseNo()).one();
            long house_id;
            if (one == null)
                continue;
            else {
                house_id = one.getId();
            }
            SharedFeeDto sharedFeeDto = SharedFeeDto.builder()
                    .houseId(house_id).liftFee(sharedFeeImportModel.getLiftFee())
                    .eleFee(sharedFeeImportModel.getEleFee()).waterFee(sharedFeeImportModel.getWaterFee())
                    .updateDate(new Date(System.currentTimeMillis())).updateUser("admin").build();
            sharedFeeDtoList.add(sharedFeeDto);
        }
        return ResultBody.ok(saveBatch(sharedFeeDtoList));
    }

    @Override
    public ResultBody insert(SharedFeeDto dto) {
        dto.setUpdateDate(new Date(System.currentTimeMillis()));
        dto.setUpdateUser("admin");
        SharedFeeDto preShared = query().eq("house_id", dto.getHouseId()).one();
        if (preShared == null) {
            return ResultBody.ok(save(dto));
        } else {
            return ResultBody.ok(update(dto, query().eq("house_id", dto.getHouseId())));
        }
    }

    @Override
    public ResultBody listByUser(String openid) {
        QueryWrapper<WechatUser> query = new QueryWrapper<>();
        query.eq("openid", openid);
        List<WechatUser> wechatUsers = wechatUserMapper.selectList(query);
        JSONArray res = new JSONArray();
        for (WechatUser wechatUser : wechatUsers) {
            JSONObject tmp = new JSONObject();
            HousingInformationDto house = housingInformationService.query().eq("id", wechatUser.getHid()).one();
            if (house == null)
                continue;
            tmp.put("house", String.format("%s#%s#%s", house.getVillageName(), house.getBuildNumber(), house.getHouseNo()));
            tmp.put("shared_fee_order", query().eq("house_id", house.getId()).list());
            res.add(tmp);
        }
        return ResultBody.ok(res);
    }

    @Override
    public int automaticPayment() {
        List<SharedFeeDto> sharedFees = query().list();
        int num = 0;
        List<HousingInformationDto> updateHouse = new ArrayList<>();
        List<SharedFeeDto> updateSharedFee = new ArrayList<>();
        for (SharedFeeDto sharedFee : sharedFees) {
            double need = sharedFee.getLiftFee() + sharedFee.getEleFee() + sharedFee.getWaterFee();
            if (need > 0) {
                HousingInformationDto house = housingInformationService.query().eq("id", sharedFee.getHouseId()).one();
                if (house == null) {
                    log.info("房屋id" + sharedFee.getHouseId() + "不存在,缴交失败");
                    continue;
                }
                double sharedBla = house.getPoolBalance();
                if (sharedBla > need) {
                    sharedBla -= need;
                    sharedFee.setEleFee(0);
                    sharedFee.setLiftFee(0);
                    sharedFee.setWaterFee(0);
                    updateSharedFee.add(sharedFee);
                    house.setPoolBalance(sharedBla);
                    updateHouse.add(house);
                    num++;
                    log.info("{}#{}#{}房屋自动公摊费缴费成功，缴交{}元，剩余{}元"
                            , house.getVillageName(), house.getBuildNumber(), house.getHouseNo()
                            , need, house.getPoolBalance()
                    );
                }
            }
        }
        housingInformationService.updateBatchById(updateHouse);
        updateBatchById(updateSharedFee);
        return num;
    }

    @Override
    public ResultBody updateByDto(SharedFeeDto dto) {
        dto.setUpdateDate(new Date(System.currentTimeMillis()));
        SharedFeeDto sharedFee = query().eq("house_id", dto.getHouseId()).one();
        if (sharedFee == null || dto.getId() == sharedFee.getId()) {
            return ResultBody.ok(updateById(dto));
        }
        {
            return ResultBody.fail("same house id");
        }
    }


}

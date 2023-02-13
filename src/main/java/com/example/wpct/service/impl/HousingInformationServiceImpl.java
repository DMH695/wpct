package com.example.wpct.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.BuildDto;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.VillageDto;
import com.example.wpct.entity.WechatUser;
import com.example.wpct.entity.vo.HousingInformationVo;
import com.example.wpct.mapper.HousingInformationMapper;
import com.example.wpct.mapper.WechatUserMapper;
import com.example.wpct.service.HousingInformationService;
import com.example.wpct.utils.ResultBody;
import com.example.wpct.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class HousingInformationServiceImpl extends ServiceImpl<HousingInformationMapper, HousingInformationDto> implements HousingInformationService {
    @Autowired
    @Lazy
    private HousingInformationMapper housingInformationMapper;

    @Autowired
    @Lazy
    private PropertyOrderServiceImpl propertyOrderService;

    @Autowired
    @Lazy
    private HousingInformationServiceImpl housingInformationService;


    @Autowired
    @Lazy
    private VillageServiceImpl villageService;

    @Autowired
    @Lazy
    private BuildServiceImpl buildService;


    @Autowired
    private WechatUserMapper wechatUserMapper;


    @Override
    public PageInfo<HousingInformationDto> listByVo(HousingInformationVo vo) {
        int pageNum = vo.getPageNum();
        int pageSize = vo.getPageSize();
        PageHelper.startPage(pageNum, pageSize);
        List<HousingInformationDto> res = query()
                .like(StringUtils.isNotEmpty(vo.getVillageName()), "village_name", vo.getVillageName())
                .eq(StringUtils.isNotEmpty(vo.getBuildNumber()), "build_number", vo.getBuildNumber())
                .eq(StringUtils.isNotEmpty(vo.getHouseNo()), "house_no", vo.getHouseNo())
                .list();
        for(HousingInformationDto housingInformationDto : res){
            QueryWrapper<WechatUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("hid",housingInformationDto.getId());
            List<WechatUser> wechatUsers = wechatUserMapper.selectList(queryWrapper);
            housingInformationDto.setBind(wechatUsers != null && wechatUsers.size() != 0);
            if (!housingInformationDto.isBind() && vo.getBound() != null)
                res.remove(housingInformationDto);
        }
        return new PageInfo<>(res);
    }

    @Override
    public List<Long> getIdsByHouseInfo(String villageName, String buildNumber, String houseNo) {
        List<Long> res = new ArrayList<>();
        List<HousingInformationDto> list = query()
                .eq(StringUtils.isNotEmpty(villageName), "village_name", villageName)
                .eq(StringUtils.isNotEmpty(buildNumber), "build_number", buildNumber)
                .eq(StringUtils.isNotEmpty(houseNo), "house_no", houseNo).list();
        for (HousingInformationDto dto : list) {
            res.add(dto.getId());
        }
        return res;
    }

    @SneakyThrows
    @Override
    public void getTemplate(HttpServletResponse response) {
        Snowflake snowflake = new Snowflake();
        List<HousingInformationDto> excelList = new ArrayList<>();
        HousingInformationDto example = HousingInformationDto.builder()
                .villageName("某某小区").buildNumber("一单元").houseNo("2103").name("张三")
                .phone("189******123").resident("福建省龙岩市").houseType("商品房").carType("汽车")
                .relation("户主").conditionNumber(1).lowNumber(0).rent(1000).area(100).trueArea(100)
                .exceedArea(0).exceedAreaUnitPrice(10).areaUnitPrice(10).carFee(100).otherFee(10)
                .stopNumberOne("123").stopNumberTwo("124").recycleFee(0).recycleRent(0).calculateRent(0)
                .calculateFee(0).discount(0).remarks("测试数据").poolBalance(1500).propertyFee(1500)
                .bindWechatUser("wechatUser").dueDate(new Timestamp(System.currentTimeMillis()).toString()).build();
        excelList.add(example);
        response.setHeader("Content-Disposition", "attachment;filename=" + snowflake.nextIdStr() + "template.xlsx");
        EasyExcel.write(response.getOutputStream())
                .head(HousingInformationDto.class)
                .sheet("importTemplate")
                .doWrite(excelList);
    }

    @SneakyThrows
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public ResultBody importHousingInformation(MultipartFile file) {
        List<HousingInformationDto> housingInformationList = EasyExcel
                .read(file.getInputStream()).head(HousingInformationDto.class).sheet().doReadSync();
        List<HousingInformationDto> saveList = new ArrayList<>();
        List<HousingInformationDto> updateList = new ArrayList<>();
        for (HousingInformationDto dto : housingInformationList) {
            HousingInformationDto one = query().eq("village_name", dto.getVillageName())
                    .eq("build_number", dto.getBuildNumber())
                    .eq("house_no", dto.getHouseNo()).one();
            if (one == null) {
                saveList.add(dto);
                saveParent(dto);
            } else {
                dto.setId(one.getId());
                updateList.add(dto);
                saveParent(dto);
            }
        }
        saveBatch(saveList);
        updateBatchById(updateList);
        return ResultBody.ok("insert: " + saveList.size() + ",update: " + updateList.size());
    }

    @Override
    public HousingInformationDto getByVbr(String villageName, String buildName, String roomNum) {
        return housingInformationMapper.getByVbr(villageName, buildName, roomNum);
    }

    @Override
    public ResultBody insert(HousingInformationDto dto) {
        HousingInformationDto one = query()
                .eq("village_name", dto.getVillageName())
                .eq("build_number", dto.getBuildNumber())
                .eq("house_no", dto.getHouseNo()).one();
        if (one == null) {
            saveParent(dto);
            return ResultBody.ok(save(dto));
        } else {
            return ResultBody.fail("house already exists");
        }
    }

    @Override
    public ResultBody updateByDto(HousingInformationDto dto) {
        HousingInformationDto house = query()
                .eq("village_name", dto.getVillageName())
                .eq("build_number", dto.getBuildNumber())
                .eq("house_no", dto.getHouseNo()).one();
        if (house == null || house.getId() == dto.getId()) {
            saveParent(dto);
            return ResultBody.ok(updateById(dto));
        } else {
            return ResultBody.fail("same house exists");
        }
    }

    @Override
    public List<HousingInformationDto> listByVillage(VillageDto village) {
        String name = village.getName();
        return query().eq("village_name", name).list();
    }

    @Override
    public List<HousingInformationDto> listByBuild(BuildDto build) {
        long villageId = build.getVillageId();
        VillageDto village = villageService.query().eq("id", villageId).one();
        String villageName = village.getName();
        String buildName = build.getName();
        return query().eq("village_name", villageName).eq("build_number", buildName).list();
    }

    @Override
    public List<HousingInformationDto> listByVillageName(String villageName) {
        return query().eq("village_name", villageName).list();
    }

    @Override
    public List<HousingInformationDto> listByBuildNumber(String buildNumber) {
        BuildDto build = buildService.query().eq("name", buildNumber).one();
        if (build == null)
            return new ArrayList<>();
        long villageId = build.getVillageId();
        VillageDto village = villageService.query().eq("id", villageId).one();
        String villageName = village.getName();
        String buildName = build.getName();
        return query().eq("village_name", villageName).eq("build_number", buildName).list();
    }

    @Override
    public ResultBody deleteByWechat(String openId, Integer houseId) {
        QueryWrapper<WechatUser> deleteQuery = new QueryWrapper<>();
        deleteQuery.eq("openid",openId).eq("hid",houseId);
        return ResultBody.ok(wechatUserMapper.delete(deleteQuery));
    }

    @Override
    public ResultBody getCostEstimate(Long hid) {
        HousingInformationDto dto = housingInformationService.query().eq("id", hid).one();
        if (dto == null){
            return ResultBody.fail("unknown hid");
        }
        if (dto.getPropertyFee() > 0){
            return ResultBody.ok(dto.getPropertyFee()/ propertyOrderService.calcCost(dto));
        }
        return ResultBody.ok(0);
    }


    private void saveParent(HousingInformationDto dto) {
        String villageName = dto.getVillageName();
        String buildNumber = dto.getBuildNumber();
        VillageDto village = villageService.query().eq("name", villageName).one();
        if (village == null) {
            village = VillageDto.builder().name(villageName).build();
            villageService.save(village);
        }
        BuildDto build = buildService.query().eq("village_id",village.getId()).eq("name", buildNumber).one();
        if (build == null) {
            build = BuildDto.builder().villageId(village.getId()).name(buildNumber).build();
            buildService.save(build);
        }
    }


}

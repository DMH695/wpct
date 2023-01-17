package com.example.wpct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.*;
import com.example.wpct.mapper.BuildMapper;
import com.example.wpct.service.BuildService;
import com.example.wpct.utils.ResultBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BuildServiceImpl extends ServiceImpl<BuildMapper, BuildDto> implements BuildService {

    @Autowired
    @Lazy
    private HousingInformationServiceImpl housingInformationService;

    @Autowired
    @Lazy
    private VillageServiceImpl villageService;

    @Autowired
    @Lazy
    private PropertyOrderServiceImpl propertyOrderService;

    @Autowired
    @Lazy
    private SharedFeeOrderServiceImpl sharedFeeOrderService;

    @Override
    public List<BuildDto> listByVillage(int villageId) {
        return query().eq("village_id", villageId).list();
    }

    @Override
    public ResultBody insert(BuildDto dto) {
        BuildDto one = query().eq("village_id", dto.getVillageId()).eq("name", dto.getName()).one();
        if (one != null){
            return ResultBody.fail("This building number already exists in this village");
        }else {
            return ResultBody.ok(save(dto));
        }
    }

    @Override
    @Transactional
    public int remove(long id) {
        BuildDto buildDto = query().eq("id", id).one();
        if (buildDto == null)
            return -1;
        long villageId = buildDto.getVillageId();
        VillageDto villageDto = villageService.query().eq("id", villageId).one();
        baseMapper.deleteById(id);
        List<HousingInformationDto> houses = housingInformationService.query()
                .eq("village_name", villageDto.getName())
                .eq("build_number", buildDto.getName()).list();
        for (HousingInformationDto house : houses) {
            QueryWrapper<PropertyOrderDto> propertyQuery = new QueryWrapper<>();
            QueryWrapper<SharedFeeOrderDto> sharedQuery = new QueryWrapper<>();
            propertyOrderService.remove(propertyQuery.eq("house_id",house.getId()));
            sharedFeeOrderService.remove(sharedQuery.eq("house_id",house.getId()));
            housingInformationService.removeById(house.getId());
        }
        return houses.size();
    }

    @Override
    @Transactional
    public ResultBody updateByDto(BuildDto dto) {
        VillageDto village = villageService.query().eq("id", dto.getVillageId()).one();
        BuildDto preBuild = query().eq("id", dto.getId()).one();
        if (preBuild == null){
            return ResultBody.fail("unknown build id");
        }else {
            List<HousingInformationDto> houseList = housingInformationService.query()
                    .eq("village_name", village.getName())
                    .eq("build_number", preBuild.getName()).list();
            for (HousingInformationDto house : houseList) {
                house.setBuildNumber(dto.getName());
            }
            return ResultBody.ok(updateById(dto));
        }

    }
}

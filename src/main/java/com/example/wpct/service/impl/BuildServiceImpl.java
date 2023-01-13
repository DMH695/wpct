package com.example.wpct.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.BuildDto;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.VillageDto;
import com.example.wpct.mapper.BuildMapper;
import com.example.wpct.service.BuildService;
import com.example.wpct.utils.ResultBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildServiceImpl extends ServiceImpl<BuildMapper, BuildDto> implements BuildService {

    @Autowired
    private HousingInformationServiceImpl housingInformationService;

    @Autowired
    private VillageServiceImpl villageService;

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
    public int remove(long id) {
        BuildDto buildDto = query().eq("id", id).one();
        long villageId = buildDto.getVillageId();
        VillageDto villageDto = villageService.query().eq("id", villageId).one();
        baseMapper.deleteById(id);
        return housingInformationService.getBaseMapper().delete(
                housingInformationService.query()
                        .eq("village_name", villageDto.getName())
                        .eq("build_number", buildDto.getName())
        );
    }

    @Override
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

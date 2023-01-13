package com.example.wpct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wpct.entity.BuildDto;
import com.example.wpct.entity.VillageDto;
import com.example.wpct.utils.ResultBody;

import java.util.List;

public interface BuildService extends IService<BuildDto> {

    List<BuildDto> listByVillage(int villageId);

    ResultBody insert(BuildDto dto);

    int remove(long id);

    ResultBody updateByDto(BuildDto dto);

}

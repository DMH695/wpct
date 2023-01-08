package com.example.wpct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wpct.entity.BuildDto;

import java.util.List;

public interface BuildService extends IService<BuildDto> {

    List<BuildDto> listByVillage(int villageId);

}

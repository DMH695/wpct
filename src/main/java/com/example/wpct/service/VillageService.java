package com.example.wpct.service;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wpct.entity.VillageDto;
import com.example.wpct.utils.ResultBody;


public interface VillageService extends IService<VillageDto> {


    ResultBody insert(VillageDto dto);


    ResultBody getTree(int pageSize,int pageNum);

    ResultBody updateByDto(VillageDto dto);

    ResultBody remove(long id);


}

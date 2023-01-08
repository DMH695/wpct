package com.example.wpct.service;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wpct.entity.VillageDto;
import com.example.wpct.utils.ResultBody;


public interface VillageService extends IService<VillageDto> {

    public ResultBody getTree(int pageSize,int pageNum);


}

package com.example.wpct.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.VillageDto;
import com.example.wpct.mapper.VillageMapper;
import com.example.wpct.service.VillageService;
import com.example.wpct.utils.ResultBody;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VillageServiceImpl extends ServiceImpl<VillageMapper, VillageDto> implements VillageService {


    @Autowired
    private BuildServiceImpl buildService;

    @Autowired
    private RoomServiceImpl roomService;

    @Override
    public ResultBody getTree(int pageSize, int pageNum) {
        Page<VillageDto> page;
        PageInfo<VillageDto> pageInfo = null;
        List<VillageDto> villages;
        if (pageSize != -1){
            page = PageHelper.startPage(pageNum,pageSize);
            villages = baseMapper.selectList(null);
            pageInfo = new PageInfo<>(villages,pageSize);
        }else {
            villages = baseMapper.selectList(null);
        }

        JSONArray tree = JSONArray.parseArray(JSON.toJSONString(villages));
        for (Object o : tree) {
            JSONObject village = ((JSONObject) o);
            JSONArray builds = JSONArray.parseArray(
                    JSON.toJSONString(buildService.listByVillage(village.getInteger("id")))
            );
            for (Object o1 : builds) {
                JSONObject build = ((JSONObject) o1);
                JSONArray rooms = JSONArray.parseArray(
                        JSON.toJSONString(roomService.listByBuild(build.getInteger("id")))
                );
                build.put("children",rooms);
            }
            village.put("children",builds);
        }
        JSONObject res = new JSONObject();
        res.put("tree",tree);
        if (pageSize != -1){
            res.put("pageInfo",pageInfo);
        }
        return ResultBody.ok(res);
    }
}

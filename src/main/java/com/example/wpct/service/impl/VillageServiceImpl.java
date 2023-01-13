package com.example.wpct.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.BuildDto;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.VillageDto;
import com.example.wpct.mapper.VillageMapper;
import com.example.wpct.service.VillageService;
import com.example.wpct.utils.ResultBody;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VillageServiceImpl extends ServiceImpl<VillageMapper, VillageDto> implements VillageService {


    @Autowired
    private BuildServiceImpl buildService;


    @Autowired
    private HousingInformationServiceImpl housingInformationService;

    /**
     * 去重插入小区
     */
    @Override
    public ResultBody insert(VillageDto dto) {
        if (query().eq("name",dto.getName()).list().size() > 0) {
            return ResultBody.fail("Village name already exists");
        }
        return ResultBody.ok(save(dto));
    }

    /**
     * 获取树形结构
     * @param pageSize 页面大小
     * @param pageNum 当前页面
     */
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
                List<HousingInformationDto> houseList = housingInformationService.query()
                        .eq("village_name", village.getString("name"))
                        .eq("build_number", build.getString("name")).list();
                JSONArray houses = new JSONArray();
                for (HousingInformationDto dto : houseList) {
                    JSONObject tmp = new JSONObject();
                    tmp.put("name",dto.getHouseNo());
                    tmp.put("id",dto.getId());
                    tmp.put("village_id",village.getString("id"));
                    tmp.put("build_id",build.getString("id"));
                    tmp.put("village_name",dto.getVillageName());
                    tmp.put("build_number",dto.getBuildNumber());
                    houses.add(tmp);
                }
                build.put("children",houses);
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

    /**
     * 去重更新小区信息，同时更新至房屋信息里
     */
    @Override
    public ResultBody updateByDto(VillageDto dto) {
        VillageDto one = query().eq("id", dto.getId()).one();
        if (one == null){
            return ResultBody.fail("unknown id");
        }else {
            VillageDto name = query().eq("name", dto.getName()).one();
            if (name == null){
                List<HousingInformationDto> houseList = housingInformationService.query().eq("village_name", one.getName()).list();
                for (HousingInformationDto houseDto : houseList) {
                    houseDto.setVillageName(dto.getName());
                }
                housingInformationService.updateBatchById(houseList);
                return ResultBody.ok(updateById(dto));
            }else {
                return ResultBody.fail("same name or already exists");
            }
        }
    }

    @Override
    public ResultBody remove(long id) {
        VillageDto village = query().eq("id", id).one();
        if (village == null){
            return ResultBody.fail("unknown id");
        }
        List<BuildDto> builds = buildService.query().eq("village_id", id).list();
        for (BuildDto build : builds) {
            housingInformationService.remove(
                    housingInformationService.query()
                            .eq("village_name",village.getName())
                            .eq("build_number",build.getName())
            );
        }
        buildService.remove(buildService.query().eq("village_id",id));
        return ResultBody.ok("All building numbers and houses under village have been deleted");
    }
}

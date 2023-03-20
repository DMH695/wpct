package com.example.wpct.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.*;
import com.example.wpct.mapper.RoleMapper;
import com.example.wpct.mapper.VillageMapper;
import com.example.wpct.service.VillageService;
import com.example.wpct.utils.ResultBody;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class VillageServiceImpl extends ServiceImpl<VillageMapper, VillageDto> implements VillageService {


    @Autowired
    private BuildServiceImpl buildService;

    @Autowired
    private HousingInformationServiceImpl housingInformationService;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    @Lazy
    private PropertyOrderServiceImpl propertyOrderService;

    @Autowired
    @Lazy
    private SharedFeeOrderServiceImpl sharedFeeOrderService;

    /**
     * 去重插入小区
     */
    @Override
    public ResultBody insert(VillageDto dto) {
        if (query().eq("name", dto.getName()).list().size() > 0) {
            return ResultBody.fail("Village name already exists");
        }
        return ResultBody.ok(save(dto));
    }

    /**
     * 获取树形结构
     *
     * @param pageSize 页面大小
     * @param pageNum  当前页面
     */
    @Override
    public ResultBody getTree(int pageSize, int pageNum) {
        PageInfo<VillageDto> pageInfo = null;
        List<VillageDto> villages = new ArrayList<>();
        if (pageSize != -1) {
            PageHelper.startPage(pageNum, pageSize);
            Subject subject = SecurityUtils.getSubject();
            SysUser sysUser = (SysUser) subject.getPrincipal();
            Role role = roleMapper.getById(sysUser.getRole());
            if (role.getData() == null ||  "".equals(role.getData())){
                villages = baseMapper.selectList(null);
            }else {
                //获取授权的数据
                String data = role.getData().replaceAll("\\[|\\]", "");
                List<String> list = Arrays.asList(data.split(","));
                /*System.out.println(data);
                List<String> dataList = Arrays.asList(data);
                System.out.println(dataList);*/
                for (String d : list){
                    QueryWrapper queryWrapper = new QueryWrapper<VillageDto>();
                    queryWrapper.eq("name",d.replace("\"","").replace("\"","").trim());
                    villages.add(baseMapper.selectOne(queryWrapper));
                }
                //villages = baseMapper.selectList(null);
            }
            pageInfo = new PageInfo<>(villages, pageSize);
        } else {
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
                    tmp.put("name", dto.getHouseNo());
                    tmp.put("id", dto.getId());
                    tmp.put("village_id", village.getString("id"));
                    tmp.put("build_id", build.getString("id"));
                    tmp.put("village_name", dto.getVillageName());
                    tmp.put("build_number", dto.getBuildNumber());
                    houses.add(tmp);
                }
                build.put("children", houses);
            }
            village.put("children", builds);
        }
        JSONObject res = new JSONObject();
        res.put("tree", tree);
        if (pageSize != -1) {
            res.put("pageInfo", pageInfo);
        }
        return ResultBody.ok(res);
    }

    /**
     * 去重更新小区信息，同时更新至房屋信息里
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public ResultBody updateByDto(VillageDto dto) {
        VillageDto one = query().eq("id", dto.getId()).one();
        if (one == null) {
            return ResultBody.fail("unknown id");
        } else {
            VillageDto name = query().eq("name", dto.getName()).one();
            if (name == null) {
                List<HousingInformationDto> houseList = housingInformationService.query().eq("village_name", one.getName()).list();
                for (HousingInformationDto houseDto : houseList) {
                    houseDto.setVillageName(dto.getName());
                }
                housingInformationService.updateBatchById(houseList);
                return ResultBody.ok(updateById(dto));
            } else {
                return ResultBody.fail("same name or already exists");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public ResultBody remove(long id) {
        VillageDto village = query().eq("id", id).one();
        if (village == null) {
            return ResultBody.fail("unknown id");
        }
        QueryWrapper<HousingInformationDto> houseDeleteQuery = new QueryWrapper<>();
        houseDeleteQuery
                .eq("village_name", village.getName());
        List<HousingInformationDto> houses = housingInformationService.list(houseDeleteQuery);
        for (HousingInformationDto house : houses) {
            QueryWrapper<PropertyOrderDto> propertyQuery = new QueryWrapper<>();
            QueryWrapper<SharedFeeOrderDto> sharedQuery = new QueryWrapper<>();
            propertyOrderService.remove(propertyQuery.eq("house_id", house.getId()));
            sharedFeeOrderService.remove(sharedQuery.eq("house_id", house.getId()));
            housingInformationService.removeById(house.getId());
        }
        QueryWrapper<BuildDto> buildDeleteQuery = new QueryWrapper<>();
        buildService.remove(buildDeleteQuery.eq("village_id", id));
        getBaseMapper().deleteById(id);
        return ResultBody.ok("All building numbers and houses under village have been deleted");
    }
}

package com.example.wpct.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.Refund;
import com.example.wpct.mapper.HousingInformationMapper;
import com.example.wpct.mapper.RefundMapper;
import com.example.wpct.service.RefundService;
import com.example.wpct.utils.page.PageRequest;
import com.example.wpct.utils.page.PageResult;
import com.example.wpct.utils.page.PageUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RefundServiceImpl implements RefundService {
    public static Page page;
    @Autowired
    RefundMapper refundMapper;
    @Autowired
    HousingInformationMapper housingInformationMapper;

    @Override
    public PageResult getAll(PageRequest pageRequest) {
        return PageUtil.getPageResult(getPageInfo(pageRequest),page);
    }

    @Override
    public void delete(int id) {
        refundMapper.delete(id);
    }

    private PageInfo<?> getPageInfo(PageRequest pageRequest) {
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();
        //设置分页数据
        page = PageHelper.startPage(pageNum,pageSize);
        List<JSONObject> res = new ArrayList<>();
        for(Refund refund : refundMapper.all()){
            HousingInformationDto housingInformationDto = housingInformationMapper.selectById(refund.getHid());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id",refund.getId());
            if (housingInformationDto != null){
                jsonObject.put("name",housingInformationDto.getName());
                jsonObject.put("villageName",housingInformationDto.getVillageName());
                jsonObject.put("buildName",housingInformationDto.getBuildNumber());
                jsonObject.put("roomNum",housingInformationDto.getHouseNo());
                jsonObject.put("phone",housingInformationDto.getPhone());
            }
            jsonObject.put("openid",refund.getOpenid());
            jsonObject.put("cost",refund.getCost());
            res.add(jsonObject);
        }
        return new PageInfo<>(res);
    }

}

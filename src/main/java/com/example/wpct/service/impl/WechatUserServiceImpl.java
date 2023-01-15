package com.example.wpct.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.wpct.entity.WechatUser;
import com.example.wpct.mapper.WechatUserMapper;
import com.example.wpct.service.WechatPayService;
import com.example.wpct.service.WechatUserService;
import com.example.wpct.utils.ResultBody;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WechatUserServiceImpl implements WechatUserService {

    @Autowired
    private WechatUserMapper wechatUserMapper;

    @Override
    public ResultBody getWechatUserInfoAndHouseInfo(int pageNum, int pageSize,String name,String telephone) {
        JSONObject result = new JSONObject();
        PageHelper.startPage(pageNum,pageSize);
        List<WechatUser> info = wechatUserMapper.getWechatUserInfoAndHouseInfo(telephone, name);
        PageInfo<WechatUser> wechatUserPageInfo = new PageInfo<>(info);
        if (wechatUserPageInfo == null){
            return ResultBody.ok("暂无信息");
        }else {
            for (int i = 0; i < info.size(); i++) {
                result.put("id",info.get(i).getId());
                result.put("nickName",info.get(i).getNickName());
                result.put("name",info.get(i).getName());
                result.put("telephone",info.get(i).getTelephone());
                result.put("openid",info.get(i).getOpenid());
                result.put("relation",info.get(i).getRelation());
                result.put("hid",info.get(i).getHid());
                result.put("villageName",info.get(i).getVillageName());
                result.put("buildName",info.get(i).getBuildNumber());
                result.put("roomNum",info.get(i).getHouseNo());
            }
        }
        return ResultBody.ok(wechatUserPageInfo);
    }

    @Override
    public ResultBody deleteWechatUser(Integer id) {
        return ResultBody.ok(wechatUserMapper.deleteById(id));
    }
}

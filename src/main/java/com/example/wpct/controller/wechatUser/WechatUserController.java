package com.example.wpct.controller.wechatUser;

import com.example.wpct.service.WechatUserService;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@Api(tags = "微信用户管理")
public class WechatUserController {

    @Autowired
    private WechatUserService wechatUserService;

    @ApiOperation("分页查找微信用户信息")
    @GetMapping("/wechatuserinfo")
    public ResultBody wechatUserInfoAndHouseInfo(int pageNum,int pageSize,String name,String telephone){
        return ResultBody.ok(wechatUserService.getWechatUserInfoAndHouseInfo(pageNum, pageSize, name, telephone));
    }

    @ApiOperation("删除微信用户信息")
    @DeleteMapping("/deletewechatuser")
    public ResultBody deleteWechatUser(Integer id){
        return ResultBody.ok(wechatUserService.deleteWechatUser(id));
    }
}

package com.example.wpct.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.wpct.entity.*;
import com.example.wpct.entity.vo.HousingInformationVo;
import com.example.wpct.mapper.PropertyOrderMapper;
import com.example.wpct.mapper.SharedFeeOrderMapper;
import com.example.wpct.mapper.WechatUserMapper;
import com.example.wpct.service.PropertyOrderService;
import com.example.wpct.service.impl.HousingInformationServiceImpl;
import com.example.wpct.service.impl.WechatServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Api(tags = "房屋信息管理模块")
@CrossOrigin
@RequestMapping("/house/info")
public class HousingInformationController {

    @Autowired
    HousingInformationServiceImpl housingInformationService;

    @Autowired
    PropertyOrderService propertyOrderService;

    @Autowired
    PropertyOrderMapper propertyOrderMapper;

    @Autowired
    SharedFeeOrderMapper sharedFeeOrderMapper;

    @Autowired
    WechatServiceImpl wechatService;

    @Autowired
    WechatUserMapper wechatUserMapper;

    @PostMapping("/insert")
    @ApiOperation("新增房屋信息")
    public ResultBody insert(@RequestBody HousingInformationDto body) {
        return ResultBody.ok(housingInformationService.insert(body));
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除指定id的房屋信息")
    public ResultBody delete(@RequestParam Integer id) {
        return ResultBody.ok(housingInformationService.removeById(id));
    }

    @PostMapping("/update")
    @ApiOperation("更新房屋信息")
    public ResultBody update(@RequestBody HousingInformationDto body){
        return ResultBody.ok(housingInformationService.updateByDto(body));
    }

    @GetMapping("/list")
    @ApiOperation("条件查询房屋信息")
    public ResultBody selectByVo(HousingInformationVo vo){
        return ResultBody.ok(housingInformationService.listByVo(vo));
    }

    @GetMapping("/get/template")
    @ApiOperation("获取房屋信息导入模板")
    public void getTemplate(HttpServletResponse response){
        housingInformationService.getTemplate(response);
    }

    @PostMapping("/import")
    @ApiOperation("通过EXCEL导入房屋信息")
    public ResultBody importHousingInfo(MultipartFile file){
        return housingInformationService.importHousingInformation(file);
    }

    @SneakyThrows
    @GetMapping("/sendHasten")
    @ApiOperation("根据房屋进行催缴")
    public Object sendHasten(@RequestParam int id){
        Double property = 0.0;
        Double shared = 0.0;
        Double total = 0.0;
        //物业费
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("payment_status",0);
        queryWrapper.eq("house_id",id);
        List<PropertyOrderDto> propertyOrderDtoList = propertyOrderMapper.selectList(queryWrapper);
        QueryWrapper queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("payment_status",0);
        queryWrapper1.eq("house_id",id);
        List<SharedFeeOrderDto> sharedFeeOrderDtoList = sharedFeeOrderMapper.selectList(queryWrapper1);
        if (propertyOrderDtoList == null && sharedFeeOrderDtoList == null){
            return ResultBody.fail("没有生成订单，无法催缴");
        }
        for (PropertyOrderDto propertyOrderDto : propertyOrderDtoList){
            property = property + propertyOrderDto.getCost();
        }
        //公摊费
        for (SharedFeeOrderDto sharedFeeOrderDto : sharedFeeOrderDtoList){
            shared = shared + sharedFeeOrderDto.getCost();
        }
        total = shared + property;
        if (total == 0){
            return ResultBody.fail("当前账户不存在欠钱行为，无需催缴");
        }
        //搜索微信用户,逐个催缴
        QueryWrapper queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("hid",id);
        List<WechatUser> wechatUsers = wechatUserMapper.selectList(queryWrapper2);
        HousingInformationDto housingInformationDto = housingInformationService.getById(id);
        int v = 0;
        int f = 0;
        for (WechatUser wechatUser : wechatUsers){
            ResultBody resultBody = wechatService.sendMsg(id,housingInformationDto.getName(),String.valueOf(total),wechatUser.getOpenid());
            if (!"0".equals(resultBody.getBody())){
                f = f + 1;
            }else {
                v = v + 1;
            }
        }
        JSONObject res = new JSONObject();
        res.put("发送成功人数",v);
        res.put("发送失败人数",f);
        return ResultBody.ok(res);
    }








}

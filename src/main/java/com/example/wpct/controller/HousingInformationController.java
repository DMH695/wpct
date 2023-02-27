package com.example.wpct.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.wpct.annotation.SysLogAnnotation;
import com.example.wpct.entity.*;
import com.example.wpct.entity.vo.HousingInformationVo;
import com.example.wpct.mapper.PropertyOrderMapper;
import com.example.wpct.mapper.SharedFeeOrderMapper;
import com.example.wpct.mapper.WechatUserMapper;
import com.example.wpct.service.PropertyOrderService;
import com.example.wpct.service.impl.HousingInformationServiceImpl;
import com.example.wpct.service.impl.SharedFeeOrderServiceImpl;
import com.example.wpct.service.impl.WechatServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
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
    @Lazy
    SharedFeeOrderServiceImpl sharedFeeOrderService;

    @Autowired
    WechatServiceImpl wechatService;

    @Autowired
    WechatUserMapper wechatUserMapper;

    @PostMapping("/insert")
    @ApiOperation("新增房屋信息")
    public ResultBody insert(@RequestBody HousingInformationDto body) {
        Subject subject = SecurityUtils.getSubject();
        SysUser sysUser = (SysUser) subject.getPrincipals();
        body.setUpdateUser(sysUser.getUsername());
        return ResultBody.ok(housingInformationService.insert(body));
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除指定id的房屋信息")
    public ResultBody delete(@RequestParam Integer id) {
        return ResultBody.ok(housingInformationService.removeById(id));
    }

    @DeleteMapping("/wechat/delete")
    @ApiOperation("手机端解绑")
    public ResultBody deleteByWechat(String openId, Integer houseId){
        return housingInformationService.deleteByWechat(openId, houseId);
    }

    @PostMapping("/update")
    @ApiOperation("更新房屋信息")
    public ResultBody update(@RequestBody HousingInformationDto body){
        Subject subject = SecurityUtils.getSubject();
        SysUser sysUser = (SysUser) subject.getPrincipals();
        body.setUpdateUser(sysUser.getUsername());
        return ResultBody.ok(housingInformationService.updateByDto(body));
    }

    @GetMapping("/list")
    @ApiOperation("条件查询房屋信息")
    @SysLogAnnotation(opModel = "房屋管理", opType = "查询", opDesc = "查询房屋列表")
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


    @GetMapping("/get/count")
    @ApiOperation("获取房屋的合计代缴")
    public ResultBody getHouseCount(long hid){
        return ResultBody.ok(propertyOrderService.houseCount(hid)+sharedFeeOrderService.houseCount(hid));
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

    @ApiOperation("预览")
    @RequestMapping(value = "/preview",method = RequestMethod.GET)
    public Object preview(@RequestParam int id){
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
        HousingInformationDto housingInformationDto = housingInformationService.getById(id);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title","物业缴费提醒");
        jsonObject.put("tableHead",housingInformationDto.getVillageName() + "-" + housingInformationDto.getBuildNumber() + "-" + housingInformationDto.getHouseNo() + "缴费提醒");
        jsonObject.put("house",housingInformationDto.getVillageName() + "-" + housingInformationDto.getBuildNumber() + "-" + housingInformationDto.getHouseNo());
        jsonObject.put("name",housingInformationDto.getName());
        jsonObject.put("type","合计金额");
        jsonObject.put("status","未缴费");
        jsonObject.put("total",total);
        jsonObject.put("remark","请即时缴交费用");
        return ResultBody.ok(jsonObject);
    }

    @ApiOperation("获取物业费、公摊费余额")
    @RequestMapping(value = "/getPoolBalance",method = RequestMethod.GET)
    public Object getPoolBalance(@RequestParam int hid){
        QueryWrapper queryWrapper = new QueryWrapper();
        Double poolBalance = housingInformationService.getById(hid).getPoolBalance();
        Double propertyFee = housingInformationService.getById(hid).getPropertyFee();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("poolBalance",poolBalance);
        jsonObject.put("propertyFee",propertyFee);
        return ResultBody.ok(jsonObject);
    }

    @GetMapping("/get/cost/estimate")
    @ApiOperation("预计能缴费到什么时间")
    public ResultBody getCostEstimate(Long hid){
        return housingInformationService.getCostEstimate(hid);
    }




}

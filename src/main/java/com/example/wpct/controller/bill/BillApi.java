package com.example.wpct.controller.bill;

import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.mapper.HousingInformationMapper;
import com.example.wpct.service.BillService;
import com.example.wpct.service.HousingInformationService;
import com.example.wpct.utils.ResultBody;
import com.example.wpct.utils.page.PageRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "账单管理")
@RestController
@RequestMapping("/bill")
public class BillApi {
    @Autowired
    BillService billService;
    @Autowired
    HousingInformationService housingInformationService;

    @ApiOperation("获取账单信息")
    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public Object getAll(@RequestParam Integer pageNum,@RequestParam Integer pageSize,
                         @RequestParam(required = false) String villageName,
                         @RequestParam(required = false) String buildName,
                         @RequestParam(required = false) String roomNum){
        PageRequest pageRequest = new PageRequest(pageNum,pageSize);
        return ResultBody.ok(billService.getAll(pageRequest,villageName,buildName,roomNum));
    }

    @ApiOperation("删除账单")
    @RequestMapping(value = "/delete",method = RequestMethod.GET)
    //@RequiresRoles("超级管理员")
    public Object delete(@RequestParam int id){
        billService.delete(id);
        return ResultBody.ok(null);
    }

    @ApiOperation("获取账单凭证")
    @GetMapping("/get/certificate")
    public ResultBody getReceiptCertificate(int id){
        return billService.getReceiptCertificate(id);
    }

    @ApiOperation("微信用户获取历史账单")
    @GetMapping("/wx/get")
    public Object getByOpenid(@RequestParam String openid,@RequestParam int hid){
        HousingInformationDto housingInformationDto = housingInformationService.getById(hid);
        if (housingInformationDto != null){
            String villageName = housingInformationDto.getVillageName();
            String buildName = housingInformationDto.getBuildNumber();
            String roomNum = housingInformationDto.getHouseNo();
            return billService.getByOpenid(openid,villageName,buildName,roomNum);
        }else {
            return ResultBody.fail("房屋信息表中不存在该房屋,请联系管理员");
        }
    }


}

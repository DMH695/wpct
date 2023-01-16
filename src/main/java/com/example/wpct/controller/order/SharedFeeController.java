package com.example.wpct.controller.order;

import com.example.wpct.entity.SharedFeeDto;
import com.example.wpct.entity.vo.SharedFeeVo;
import com.example.wpct.service.impl.SharedFeeServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/shared/fee")
@Api(tags = "公摊费管理模块(弃用)", hidden = true)
public class SharedFeeController {

    @Autowired
    private SharedFeeServiceImpl sharedFeeService;

    @ApiOperation("手动添加公摊费订单表")
    @PostMapping("/insert")
    public ResultBody insert(SharedFeeDto dto){
        return sharedFeeService.insert(dto);
    }

    @ApiOperation("手动删除公摊费订单表")
    @DeleteMapping("/delete")
    public ResultBody remove(@RequestParam Integer id){
        return ResultBody.ok(sharedFeeService.removeById(id));
    }

    @ApiOperation("更新公台肥订单表信息")
    @PostMapping("/update")
    public ResultBody update(SharedFeeDto dto){
        return sharedFeeService.updateByDto(dto);
    }

    @ApiOperation("根据条件获取公摊费订单列表")
    @GetMapping("/list")
    public ResultBody list(SharedFeeVo vo){
        return sharedFeeService.list(vo);
    }

    @ApiOperation("获取公摊费EXCEL导入模板")
    @GetMapping("/get/template")
    public void getTemplate(HttpServletResponse response){
        sharedFeeService.getTemplate(response);
    }

    @ApiOperation("EXCEL导入公摊费")
    @PostMapping("/import")
    public ResultBody importSharedFee(MultipartFile file){
        return sharedFeeService.importSharedFee(file);
    }

    @GetMapping("/wechat/user/get")
    public ResultBody listByUser(@RequestParam("openid") String openid){
        return sharedFeeService.listByUser(openid);
    }

    @ApiOperation("手动缴交公摊费")
    @GetMapping("/manual/payment")
    public ResultBody manualPayment(){
        return ResultBody.ok(sharedFeeService.automaticPayment());
    }





}

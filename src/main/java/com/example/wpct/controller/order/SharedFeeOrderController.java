package com.example.wpct.controller.order;

import com.example.wpct.entity.SharedFeeOrderDto;
import com.example.wpct.entity.vo.SharedFeeOrderVo;
import com.example.wpct.service.impl.SharedFeeOrderServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/shared/fee/order")
@Api(tags = "公摊费订单管理模块")
public class SharedFeeOrderController {

    @Autowired
    private SharedFeeOrderServiceImpl sharedFeeOrderService;

    @PostMapping("/insert")
    @ApiOperation("手动添加物业费订单")
    public ResultBody insert(SharedFeeOrderDto dto){
        return sharedFeeOrderService.insert(dto);
    }

    @ApiOperation("手动删除订单")
    @DeleteMapping("/remove")
    public ResultBody remove(Long orderNo){
        return ResultBody.ok(sharedFeeOrderService.removeById(orderNo));
    }

    @ApiOperation("手动更新订单")
    @PostMapping("/update")
    public ResultBody update(SharedFeeOrderDto dto){
        return ResultBody.ok(sharedFeeOrderService.updateById(dto));
    }

    @ApiOperation("订单列表")
    @GetMapping("/list")
    public ResultBody list(SharedFeeOrderVo vo){
        return sharedFeeOrderService.list(vo);
    }

    @ApiOperation("获取公摊费订单导入模板")
    @GetMapping("/get/template")
    public void getTemplate(HttpServletResponse response){
        sharedFeeOrderService.getTemplate(response);
    }

    @ApiOperation("导入公摊费订单")
    @PostMapping("/import")
    public ResultBody importOrder(MultipartFile file){
        return sharedFeeOrderService.importOrder(file);
    }

    @ApiOperation("执行扣费服务，自动从公摊费余额缴交公摊费订单")
    @PostMapping("/execute/deduction")
    public ResultBody executeDeduction(){
        return sharedFeeOrderService.executeDeduction();
    }



}

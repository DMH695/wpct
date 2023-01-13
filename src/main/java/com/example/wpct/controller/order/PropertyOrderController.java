package com.example.wpct.controller.order;

import com.example.wpct.entity.PropertyOrderDto;
import com.example.wpct.entity.vo.PropertyOrderVo;
import com.example.wpct.service.impl.PropertyOrderServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@Api(tags = "物业费订单管理模块")
@RequestMapping("/property/order")
public class PropertyOrderController {

    @Autowired
    private PropertyOrderServiceImpl propertyOrderService;

    @ApiOperation("手动添加订单")
    @PostMapping("/insert")
    public ResultBody insert(PropertyOrderDto dto){
        return ResultBody.ok(propertyOrderService.save(dto));
    }


    @ApiOperation("手动删除订单")
    @DeleteMapping("/remove")
    public ResultBody remove(@RequestParam Integer id){
        return ResultBody.ok(propertyOrderService.removeById(id));
    }

    @ApiOperation("手动更新订单信息")
    @PostMapping("/update")
    public ResultBody update(PropertyOrderDto dto){
        return ResultBody.ok(propertyOrderService.updateById(dto));
    }

    @ApiOperation("获取订单列表")
    @GetMapping("/list")
    public ResultBody list(PropertyOrderVo vo){
        return propertyOrderService.list(vo);
    }

    @ApiOperation("用户获取自己的物业费订单列表")
    public ResultBody listByUser(@RequestParam("openid") String openid){
        return propertyOrderService.listByUser(openid);
    }

//    @GetMapping("/get/template")
//    @ApiOperation("下载导入模板（未完成）")
//    public void getTemplate(HttpServletResponse response){
//        propertyOrderService.getTemplate(response);
//    }

//    @ApiOperation("通过EXCEL表格导入物业费订单（未完成）")
//    @PostMapping("/import")
//    public ResultBody importOrder(MultipartFile file){
//        return propertyOrderService.importPropertyOrder(file);
//    }

}

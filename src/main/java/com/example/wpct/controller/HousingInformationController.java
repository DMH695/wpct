package com.example.wpct.controller;


import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.vo.HousingInformationVo;
import com.example.wpct.service.impl.HousingInformationServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@Api(tags = "房屋信息管理模块")
@CrossOrigin
@RequestMapping("/house/info")
public class HousingInformationController {

    @Autowired
    HousingInformationServiceImpl housingInformationService;

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








}

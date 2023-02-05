package com.example.wpct.controller;

import com.example.wpct.service.ReportService;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
@Api(tags = "数据报表模块")
@CrossOrigin
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/get")
    @ApiOperation("获取报表")
    public ResultBody getReport(){
        return reportService.getReport();
    }
}

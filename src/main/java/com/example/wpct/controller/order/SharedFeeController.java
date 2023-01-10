package com.example.wpct.controller.order;

import com.example.wpct.service.impl.SharedFeeServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shared/fee")
@Api(tags = "公摊费管理模块")
public class SharedFeeController {

    @Autowired
    private SharedFeeServiceImpl sharedFeeService;

    @PostMapping("/insert")
    public ResultBody insert(){
        return null;
    }



}

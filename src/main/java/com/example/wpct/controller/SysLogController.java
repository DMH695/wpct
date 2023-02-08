package com.example.wpct.controller;

import com.example.wpct.entity.vo.LogVo;
import com.example.wpct.service.impl.SysLogServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "日志管理模块")
@CrossOrigin
@RequestMapping("/sys/log")
public class SysLogController {

    @Autowired
    private SysLogServiceImpl sysLogService;

    @ApiOperation("删除日志")
    @DeleteMapping("/remove")
    public ResultBody remove(@RequestParam Long id){
        return ResultBody.ok(sysLogService.removeById(id));
    }

    @GetMapping("/list")
    public ResultBody list(@RequestBody LogVo vo){
        return sysLogService.list(vo);
    }

}

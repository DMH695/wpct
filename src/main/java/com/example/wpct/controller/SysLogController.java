package com.example.wpct.controller;

import com.example.wpct.entity.SysUser;
import com.example.wpct.entity.vo.LogVo;
import com.example.wpct.service.RoleService;
import com.example.wpct.service.impl.SysLogServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "日志管理模块")
@CrossOrigin
@RequestMapping("/sys/log")
public class SysLogController {

    @Autowired
    private SysLogServiceImpl sysLogService;
    @Autowired
    RoleService roleService;

    @SneakyThrows
    @ApiOperation("删除日志")
    @DeleteMapping("/remove")
    public ResultBody remove(@RequestParam Long id){
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        if (user == null){
            throw new Exception("请先登录");
        }
        String permission = roleService.getById(user.getRole()).getPermission();
        if (!permission.contains("删除")) {
            return ResultBody.ok("您没有删除权限");
        }
        return ResultBody.ok(sysLogService.removeById(id));
    }

    @GetMapping("/list")
    @ApiOperation("获取日志列表")
    public ResultBody list(@RequestBody LogVo vo){
        return sysLogService.list(vo);
    }

}

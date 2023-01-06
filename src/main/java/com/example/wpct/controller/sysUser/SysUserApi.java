package com.example.wpct.controller.sysUser;

import com.example.wpct.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "系统用户管理")
@RestController
@RequestMapping("/sys")
public class SysUserApi {
    @Autowired
    SysUserService sysUserService;

    /*@ApiOperation("返回用户列表")
    @RequestMapping(value = "/user/list",method = RequestMethod.GET)
    public Object list(){

    }*/
}

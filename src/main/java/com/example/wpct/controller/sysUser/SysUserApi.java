package com.example.wpct.controller.sysUser;

import com.example.wpct.entity.SysUser;
import com.example.wpct.service.SysUserService;
import com.example.wpct.utils.ResultBody;
import com.example.wpct.utils.page.PageRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@Api(tags = "系统用户管理")
@RestController
@RequestMapping("/sys")
public class SysUserApi {
    @Autowired
    SysUserService sysUserService;

    /**
     * 返回id、name、username、role
     * @return
     */
    @ApiOperation("返回用户列表")
    @RequestMapping(value = "/user/list",method = RequestMethod.GET)
    //@RequiresRoles("超级管理员")
    public Object list(@RequestParam Integer pageNum,@RequestParam Integer pageSize){
        PageRequest pageRequest = new PageRequest(pageNum,pageSize);
        return ResultBody.ok(sysUserService.getAll(pageRequest));
    }

    /**
     * 输入name、username、password、role
     */
    @ApiOperation("新增系统用户")
    @RequestMapping(value = "/user/insert",method = RequestMethod.POST)
    //@RequiresRoles("超级管理员")
    public Object insert(@RequestBody SysUser sysUser){
        String password = sysUser.getPassword();
        sysUser.setPassword(DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8)));
        sysUserService.insert(sysUser);
        return ResultBody.ok(null);
    }

    /**
     * 输入name、username、password、role、id
     */
    @ApiOperation("修改系统用户")
    @RequestMapping(value = "/user/update",method = RequestMethod.POST)
    //@RequiresRoles("超级管理员")
    public Object update(@RequestBody SysUser sysUser){
        if (sysUser.getId() == null){
            return ResultBody.fail("缺少参数id");
        }
        String password = sysUser.getPassword();
        sysUser.setPassword(DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8)));
        sysUserService.insert(sysUser);
        return ResultBody.ok(null);
    }

    @ApiOperation("删除系统用户")
    @RequestMapping(value = "/user/delete",method = RequestMethod.GET)
    //@RequiresRoles("超级管理员")
    public Object delete(@RequestParam int id){
        sysUserService.delete(id);
        return ResultBody.ok(null);
    }

    @ApiOperation("根据id查询用户")
    @RequestMapping(value = "/user/getById",method = RequestMethod.GET)
    //@RequiresRoles("超级管理员")
    public Object getById(@RequestParam int id){
        return ResultBody.ok(sysUserService.getById(id));
    }
}

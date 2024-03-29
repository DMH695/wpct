package com.example.wpct.controller.sysUser;

import com.example.wpct.entity.Role;
import com.example.wpct.entity.SysUser;
import com.example.wpct.service.RoleService;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/role")
@Api(tags = "角色管理")
public class RoleApi {
    @Autowired
    RoleService roleService;

    @ApiOperation("返回所有角色")
    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public Object all(){
        return ResultBody.ok(roleService.all());
    }

    @ApiOperation("根据id返回角色信息")
    @RequestMapping(value = "/getById",method = RequestMethod.GET)
    public Object getById(@RequestParam int id){
        return ResultBody.ok(roleService.getById(id));
    }

    @ApiOperation("新增角色")
    @RequestMapping(value = "insert",method = RequestMethod.POST)
    public Object insert(@RequestBody Role role){
        String name = role.getName();
        String permission = role.getPermission();
        roleService.insert(name, permission);
        return ResultBody.ok(null);
    }

    @ApiOperation("更新角色信息")
    @RequestMapping(value = "/update",method = RequestMethod.POST)
    public Object update(@RequestBody Role role){
        int id = role.getId();
        String name = role.getName();
        String permission = role.getPermission();
        roleService.update(id,name,permission);
        return ResultBody.ok(null);
    }
    @SneakyThrows
    @ApiOperation("删除角色")
    @RequestMapping(value = "/delete",method = RequestMethod.GET)
    public Object delete(@RequestParam int id){
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        if (user == null){
            throw new Exception("请先登录");
        }
        String permission = roleService.getById(user.getRole()).getPermission();
        if (!permission.contains("删除")) {
            return ResultBody.ok("您没有删除权限");
        }
        roleService.delete(id);
        return ResultBody.ok(null);
    }

    @ApiOperation("授权数据")
    @RequestMapping(value = "/auth/data",method = RequestMethod.POST)
    public Object authData(@RequestParam String[] villageNames,@RequestParam int roleId){
        List<String> list = Arrays.stream(villageNames).collect(Collectors.toList());
        roleService.authData(list.toString(),roleId);
        return new ResultBody(true,200,null);
    }
}

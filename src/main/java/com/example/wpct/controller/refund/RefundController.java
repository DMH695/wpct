package com.example.wpct.controller.refund;

import com.example.wpct.entity.SysUser;
import com.example.wpct.service.RefundService;
import com.example.wpct.service.RoleService;
import com.example.wpct.utils.ResultBody;
import com.example.wpct.utils.page.PageRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@Api(tags = "退款记录")
@RestController
@RequestMapping("/refund")
public class RefundController {
    @Autowired
    RefundService refundService;
    @Autowired
    RoleService roleService;

    @ApiOperation("获取退款记录")
    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public Object all(@RequestParam Integer pageNum,@RequestParam Integer pageSize){
        PageRequest pageRequest = new PageRequest(pageNum,pageSize);
        return ResultBody.ok(refundService.getAll(pageRequest));
    }

    @SneakyThrows
    @ApiOperation("删除退款记录")
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
        refundService.delete(id);
        return ResultBody.ok(null);
    }
}

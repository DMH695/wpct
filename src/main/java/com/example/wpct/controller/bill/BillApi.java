package com.example.wpct.controller.bill;

import com.example.wpct.service.BillService;
import com.example.wpct.utils.ResultBody;
import com.example.wpct.utils.page.PageRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "账单管理")
@RestController
@RequestMapping("/bill")
public class BillApi {
    @Autowired
    BillService billService;

    @ApiOperation("获取账单信息")
    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public Object getAll(@RequestParam Integer pageNum,@RequestParam Integer pageSize,
                         @RequestParam(required = false) String villageName,
                         @RequestParam(required = false) String buildName,
                         @RequestParam(required = false) String roomNum){
        PageRequest pageRequest = new PageRequest(pageNum,pageSize);
        return ResultBody.ok(billService.getAll(pageRequest,villageName,buildName,roomNum));
    }

    @ApiOperation("删除账单")
    @RequestMapping(value = "/delete",method = RequestMethod.GET)
    //@RequiresRoles("超级管理员")
    public Object delete(@RequestParam int id){
        billService.delete(id);
        return ResultBody.ok(null);
    }

}
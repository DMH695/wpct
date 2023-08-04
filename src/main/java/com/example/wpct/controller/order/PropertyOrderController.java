package com.example.wpct.controller.order;

import com.alibaba.fastjson.JSONObject;
import com.example.wpct.entity.PropertyOrderDto;
import com.example.wpct.entity.RollBack;
import com.example.wpct.entity.SharedFeeOrderDto;
import com.example.wpct.entity.SysUser;
import com.example.wpct.entity.vo.PropertyOrderVo;
import com.example.wpct.service.RoleService;
import com.example.wpct.service.RollBackService;
import com.example.wpct.service.impl.PropertyOrderServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@RestController
@Api(tags = "物业费订单管理模块")
@RequestMapping("/property/order")
@Slf4j
public class PropertyOrderController {

    @Autowired
    private PropertyOrderServiceImpl propertyOrderService;
    @Autowired
    RollBackService rollBackService;
    @Autowired
    RoleService roleService;

    @ApiOperation("手动添加订单")
    @PostMapping("/insert")
    public ResultBody insert(PropertyOrderDto dto){
        return propertyOrderService.insert(dto);
    }


    @SneakyThrows
    @ApiOperation("手动删除订单")
    @DeleteMapping("/remove")
    public ResultBody remove(@RequestParam Long orderNo){
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        if (user == null){
            throw new Exception("请先登录");
        }
        String permission = roleService.getById(user.getRole()).getPermission();
        if (!permission.contains("删除")) {
            return ResultBody.ok("您没有删除权限");
        }
        return ResultBody.ok(propertyOrderService.removeById(orderNo));
    }

    @SneakyThrows
    @ApiOperation("手动更新订单信息")
    @PostMapping("/update")
    public ResultBody update(PropertyOrderDto dto){
        //dto.setOrderNo(orderNo);
        log.info(dto.toString());
        //新增一条备份记录，并关联外键
        RollBack rollBack = new RollBack();
        PropertyOrderDto dto1 = propertyOrderService.getById(dto.getOrderNo());
        rollBack.setPaymentStatus(dto.getPaymentStatus());
        rollBack.setDetail(dto.getCostDetail());
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        if (user == null){
            throw new Exception("请先登录");
        }
        rollBack.setUid(user.getId());
        rollBack.setUid(1);
        String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime());
        rollBack.setTime(time);
        rollBack.setCost(dto.getCost());
        rollBackService.insert(rollBack);
        //外键关联
        dto.setRid(rollBack.getId());
        propertyOrderService.updateRid(dto.getOrderNo(),rollBack.getId());
        return ResultBody.ok(null);
    }

    @ApiOperation("获取订单列表")
    @GetMapping("/list")
    public ResultBody list(PropertyOrderVo vo){
        return propertyOrderService.list(vo);
    }

    @ApiOperation("通过房屋id获取订单")
    @GetMapping("/list/house/id")
    public ResultBody listByHouseId(long houseId){
        return propertyOrderService.listByHouseId(houseId);
    }

    @ApiOperation("用户获取自己的物业费订单列表")
    @GetMapping("/wechat/user/get")
    public ResultBody listByUser(@RequestParam("openid") String openid){
        return propertyOrderService.listByUser(openid);
    }

    @SneakyThrows
    @ApiOperation("通过订单号返回两组数据")
    @GetMapping("/check")
    public Object check(@RequestParam Long orderNo){
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        if (user == null){
            throw new Exception("请先登录");
        }
        String permission = roleService.getById(user.getRole()).getPermission();
        if (!permission.contains("订单审批")){
            return ResultBody.fail("权限不足,无法审批，请添加订单审批权限");
        }
        JSONObject res = new JSONObject();
        Map<String,Object> map1 = new HashMap<>();
        Map<String,Object> map2 = new HashMap<>();
        PropertyOrderDto dto = propertyOrderService.getById(orderNo);
        RollBack rollBack = rollBackService.getById(dto.getRid());
        map1.put("payment_status",dto.getPaymentStatus());
        map1.put("detal",dto.getCostDetail());
        map2.put("payment_status",rollBack.getPaymentStatus());
        map2.put("detail",rollBack.getDetail());
        res.put("old",map1);
        res.put("new",map2);
        return ResultBody.ok(res);
    }
//    @GetMapping("/get/template")
//    @ApiOperation("下载导入模板（未完成）")
//    public void getTemplate(HttpServletResponse response){
//        propertyOrderService.getTemplate(response);
//    }

//    @ApiOperation("通过EXCEL表格导入物业费订单（未完成）")
//    @PostMapping("/import")
//    public ResultBody importOrder(MultipartFile file){
//        return propertyOrderService.importPropertyOrder(file);
//    }
    @SneakyThrows
    @ApiOperation("订单审批,审批通过")
    @GetMapping("/check/true")
    public Object checkTrue(@RequestParam Long orderNo){
        //将rid设为null，将数据进行更新
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        if (user == null){
            throw new Exception("请先登录");
        }
        String permission = roleService.getById(user.getRole()).getPermission();
        //获取原有的订单
        PropertyOrderDto dto1 = propertyOrderService.selectById(orderNo);
        Integer rid = dto1.getRid();
        if (permission.contains("订单审批")){
            if (rid == null){
                return ResultBody.fail("rid为null，已经审批通过，无法再次审批");
            }
            //获取审批通过后的订单
            RollBack rollBack = rollBackService.getById(dto1.getRid());
            //更新后的订单
            PropertyOrderDto dto = new PropertyOrderDto();
            dto.setOrderNo(orderNo);
            dto.setCost(dto1.getCost());
            dto.setHouseId(dto1.getHouseId());
            dto.setPaymentStatus(rollBack.getPaymentStatus());
            dto.setCostDetail(rollBack.getDetail());
            dto.setCost(rollBack.getCost());
            propertyOrderService.updateById(dto);
            propertyOrderService.updateRid(orderNo,null);
            return ResultBody.ok(null);
        }else {
            return ResultBody.fail("权限不足,无法审批，请添加订单审批权限");
        }

    }

    @SneakyThrows
    @ApiOperation("订单审批,审批不通过")
    @GetMapping("/check/false")
    public Object checkFalse(@RequestParam Long orderNo){
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        if (user == null){
            throw new Exception("请先登录");
        }
        String permission = roleService.getById(user.getRole()).getPermission();
        if (!permission.contains("订单审批")){
            return ResultBody.fail("权限不足,无法审批，请添加订单审批权限");

        }
        //将rid设为null，不需要更新数据
        PropertyOrderDto dto1 = propertyOrderService.selectById(orderNo);
        Integer rid = dto1.getRid();
        if (rid == null){
            return ResultBody.fail("rid为null，已经审批通过，无法再次审批");
        }
        propertyOrderService.updateRid(orderNo,null);
        return ResultBody.ok(null);
    }
}

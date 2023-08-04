package com.example.wpct.controller.order;

import com.alibaba.fastjson.JSONObject;
import com.example.wpct.entity.PropertyOrderDto;
import com.example.wpct.entity.RollBack;
import com.example.wpct.entity.SharedFeeOrderDto;
import com.example.wpct.entity.SysUser;
import com.example.wpct.entity.vo.SharedFeeOrderVo;
import com.example.wpct.service.RoleService;
import com.example.wpct.service.RollBackService;
import com.example.wpct.service.impl.SharedFeeOrderServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
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
@RequestMapping("/shared/fee/order")
@Api(tags = "公摊费订单管理模块")
public class SharedFeeOrderController {

    @Autowired
    private SharedFeeOrderServiceImpl sharedFeeOrderService;
    @Autowired
    private RollBackService rollBackService;
    @Autowired
    RoleService roleService;
    @PostMapping("/insert")
    @ApiOperation("手动添加物业费订单")
    public ResultBody insert(SharedFeeOrderDto dto){
        return sharedFeeOrderService.insert(dto);
    }

    @SneakyThrows
    @ApiOperation("手动删除订单")
    @DeleteMapping("/remove")
    public ResultBody remove(Long orderNo){
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        if (user == null){
            throw new Exception("请先登录");
        }
        String permission = roleService.getById(user.getRole()).getPermission();
        if (!permission.contains("删除")) {
            return ResultBody.ok("您没有删除权限");
        }
        return ResultBody.ok(sharedFeeOrderService.removeById(orderNo));
    }

    @ApiOperation("手动更新订单")
    @PostMapping("/update")
    public ResultBody update(SharedFeeOrderDto dto) throws Exception {
        //新增一个备份记录
        RollBack rollBack = new RollBack();
        SharedFeeOrderDto dto1 = sharedFeeOrderService.getById(dto.getOrderNo());
        rollBack.setPaymentStatus(dto.getPaymentStatus());
        rollBack.setDetail(dto.getCostDetail());
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        if (user == null){
            throw new Exception("请先登录");
        }
        rollBack.setUid(user.getId());
        String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime());
        rollBack.setTime(time);
        rollBack.setCost(dto.getCost());
        rollBackService.insert(rollBack);
        //外键关联
        dto.setRid(rollBack.getId());
        sharedFeeOrderService.updateRid(dto.getOrderNo(),rollBack.getId());
        return ResultBody.ok(null);
    }

    @ApiOperation("订单列表")
    @GetMapping("/list")
    public ResultBody list(SharedFeeOrderVo vo){
        return sharedFeeOrderService.list(vo);
    }

    @ApiOperation("获取公摊费订单导入模板")
    @GetMapping("/get/template")
    public void getTemplate(HttpServletResponse response){
        sharedFeeOrderService.getTemplate(response);
    }

    @ApiOperation("导入公摊费订单")
    @PostMapping("/import")
    public ResultBody importOrder(MultipartFile file){
        return sharedFeeOrderService.importOrder(file);
    }

    @ApiOperation("执行扣费服务，自动从公摊费余额缴交公摊费订单")
    @PostMapping("/execute/deduction")
    public ResultBody executeDeduction(){
        return sharedFeeOrderService.executeDeduction();
    }

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
        SharedFeeOrderDto dto1 = sharedFeeOrderService.selectById(orderNo);
        Integer rid = dto1.getRid();
        if (permission.contains("订单审批")){
            if (rid == null){
                return ResultBody.fail("rid为null，已经审批通过，无法再次审批");
            }
            //获取审批通过后的订单
            RollBack rollBack = rollBackService.getById(dto1.getRid());
            //更新后的订单
            SharedFeeOrderDto dto = new SharedFeeOrderDto();
            dto.setOrderNo(orderNo);
            dto.setCost(dto1.getCost());
            dto.setHouseId(dto1.getHouseId());
            dto.setPaymentStatus(rollBack.getPaymentStatus());
            dto.setCostDetail(rollBack.getDetail());
            dto.setCost(rollBack.getCost());
            sharedFeeOrderService.updateById(dto);
            sharedFeeOrderService.updateRid(orderNo,null);
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
        SharedFeeOrderDto dto1 = sharedFeeOrderService.selectById(orderNo);
        Integer rid = dto1.getRid();
        if (rid == null){
            return ResultBody.fail("rid为null，已经审批通过，无法再次审批");
        }
        sharedFeeOrderService.updateRid(orderNo,null);
        return ResultBody.ok(null);
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
        SharedFeeOrderDto dto = sharedFeeOrderService.getById(orderNo);
        RollBack rollBack = rollBackService.getById(dto.getRid());
        map1.put("payment_status",dto.getPaymentStatus());
        map1.put("detal",dto.getCostDetail());
        map2.put("payment_status",rollBack.getPaymentStatus());
        map2.put("detail",rollBack.getDetail());
        res.put("old",map1);
        res.put("new",map2);
        return ResultBody.ok(res);
    }
}

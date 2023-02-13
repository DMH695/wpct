package com.example.wpct.controller;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.wpct.entity.ExamineDto;
import com.example.wpct.entity.SysUser;
import com.example.wpct.service.RoleService;
import com.example.wpct.service.impl.ExamineServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "意见处理")
@CrossOrigin
@RequestMapping("/examine/info")
public class ExamineController {
    @Autowired
    private ExamineServiceImpl examineService;
    @Autowired
    RoleService roleService;

    @ApiOperation("新增处理（审批）")
    @PostMapping("/add")
    public ResultBody addExamine(@RequestParam String openid,
                                 @RequestParam String examineContent,
                                 @RequestParam int hid
                                 ) {
        return examineService.addExamine(openid,examineContent,hid);
    }
    @ApiOperation("后台处理（意见,审批）列表")
    @GetMapping("/list")
    public ResultBody listExamine(@RequestParam int pageNum,@RequestParam int pageSize) {
        return ResultBody.ok(examineService.listExamine(pageNum,pageSize));
    }

    @ApiOperation("微信用户历史 记录处理（审批）")
    @PostMapping("/wxlist")
    public ResultBody userExamine(@RequestBody String openid) {
        return examineService.userExamineList(openid);
    }

    @ApiOperation("微信用户根据id查看意见的具体信息")
    @GetMapping("/getById")
    public Object getById(@RequestParam int id){
        QueryWrapper queryWrapper = new QueryWrapper();
        return ResultBody.ok(examineService.getById(id));
    }

    /**
     * 后台管理员处理用户意见
     */
    @ApiOperation("处理意见")
    @PostMapping("/solu")
    public ResultBody examineHandle(@RequestParam Integer id,@RequestParam String resolveMsg) {
        examineService.soluExamine(id,resolveMsg);
        return ResultBody.ok(null);
    }

    @ApiOperation("审批")
    @PostMapping("/approve")
    //@RequiresRoles("超级管理员")
    public ResultBody approve(@RequestParam Integer id) {
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        if (user != null){
            if (roleService.getById(user.getRole()).getPermission().contains("意见管理:审批")){
                examineService.approval(id);
                return ResultBody.ok(null);
            }else {
                return ResultBody.fail("权限不足");
            }
        }
        return ResultBody.ok(null);
    }

    @ApiOperation("审批——不通过")
    @GetMapping("/noPass")
    public Object noPass(@RequestParam Integer id){
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        if (user != null){
            if (roleService.getById(user.getRole()).getPermission().contains("意见管理:审批")){
                examineService.noPass(id);
                return ResultBody.ok(null);
            }else {
                return ResultBody.fail("权限不足");
            }
        }
        return ResultBody.ok(null);
    }

    @ApiOperation("获取新消息")
    @GetMapping("/getMessage")
    public Object getMessage(){
        Subject subject = SecurityUtils.getSubject();
        SysUser user = (SysUser)subject.getPrincipal();
        String permission = roleService.getById(user.getRole()).getPermission();
        if (permission.contains("意见管理:处理") & !permission.contains("意见管理:审批")){
            //返回应处理数量
            JSONObject jsonObject = new JSONObject();
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("resolve_handle","");
            jsonObject.put("soluCount",examineService.list(queryWrapper).size());
            return ResultBody.ok(jsonObject);
        }else if (!permission.contains("意见管理:处理") & permission.contains("意见管理:审批")){
            //返回应审批数量
            JSONObject jsonObject = new JSONObject();
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("approval_status","");
            jsonObject.put("approvalCount",examineService.list(queryWrapper).size());
            return ResultBody.ok(jsonObject);
        }else if (permission.contains("意见管理:处理") & permission.contains("意见管理:审批")){
            //返回二者的数量
            JSONObject jsonObject = new JSONObject();
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("resolve_handle","");
            jsonObject.put("soluCount",examineService.list(queryWrapper).size());
            QueryWrapper queryWrapper1 = new QueryWrapper();
            queryWrapper1.eq("approval_status","");
            jsonObject.put("approvalCount",examineService.list(queryWrapper1).size());
            return ResultBody.ok(jsonObject);
        }else {
            return ResultBody.ok(null);
        }
    }
}

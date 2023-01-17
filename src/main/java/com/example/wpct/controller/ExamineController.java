package com.example.wpct.controller;
import com.example.wpct.entity.ExamineDto;
import com.example.wpct.service.impl.ExamineServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "意见处理")
@CrossOrigin
@RequestMapping("/examine/info")
public class ExamineController {
    @Autowired
    private ExamineServiceImpl examineService;


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
        examineService.approval(id);
        return ResultBody.ok(null);
    }
}

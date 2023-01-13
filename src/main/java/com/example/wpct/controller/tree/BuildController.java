package com.example.wpct.controller.tree;


import com.example.wpct.entity.BuildDto;
import com.example.wpct.service.impl.BuildServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/build")
@Api(tags = "楼栋管理")
@CrossOrigin
public class BuildController {

    @Autowired
    private BuildServiceImpl buildService;

    @ApiOperation("增加楼栋")
    @PostMapping("/insert")
    public ResultBody insert(BuildDto dto){
        return ResultBody.ok(buildService.insert(dto));
    }

    @ApiOperation("通过id删除楼栋")
    @DeleteMapping("/remove")
    public ResultBody remove(@RequestParam Integer id){
        return ResultBody.ok(buildService.remove(id));
    }

    @ApiOperation("修改楼栋信息")
    @PostMapping("/update")
    public ResultBody update(BuildDto dto){
        return ResultBody.ok(buildService.updateByDto(dto));
    }
}

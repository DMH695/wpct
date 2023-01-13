package com.example.wpct.controller.tree;

import com.example.wpct.entity.VillageDto;
import com.example.wpct.service.impl.VillageServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "小区管理")
@RequestMapping("/village")
@CrossOrigin
public class VillageController {

    @Autowired
    private VillageServiceImpl villageService;

    @ApiOperation("增加小区")
    @PostMapping("/insert")
    public ResultBody insert(VillageDto dto){
        return villageService.insert(dto);
    }

    @ApiOperation("通过id删除小区")
    @DeleteMapping("/remove")
    public ResultBody remove(@RequestParam Integer id){
        return villageService.remove(id);
    }

    @ApiOperation("修改小区信息")
    @PostMapping("/update")
    public ResultBody update(VillageDto dto){
        return villageService.updateByDto(dto);
    }

    @ApiOperation("获取树形结构")
    @GetMapping("/tree")
    public ResultBody tree(int pageNum,int pageSize){
        return villageService.getTree(pageSize,pageNum);
    }

}

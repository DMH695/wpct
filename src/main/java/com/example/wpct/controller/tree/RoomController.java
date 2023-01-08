package com.example.wpct.controller.tree;

import com.example.wpct.entity.RoomDto;
import com.example.wpct.service.impl.RoomServiceImpl;
import com.example.wpct.utils.ResultBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Room")
@Api(tags = "房屋管理")
@CrossOrigin
public class RoomController {

    @Autowired
    private RoomServiceImpl RoomService;

    @ApiOperation("增加房屋")
    @PostMapping("/insert")
    public ResultBody insert(RoomDto dto){
        return ResultBody.ok(RoomService.save(dto));
    }

    @ApiOperation("通过id删除房屋")
    @DeleteMapping("/remove")
    public ResultBody remove(@RequestParam Integer id){
        return ResultBody.ok(RoomService.removeById(id));
    }

    @ApiOperation("修改房屋信息")
    @PostMapping("/update")
    public ResultBody update(RoomDto dto){
        return ResultBody.ok(RoomService.updateById(dto));
    }
}


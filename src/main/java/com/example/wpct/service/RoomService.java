package com.example.wpct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wpct.entity.RoomDto;

import java.util.List;

public interface RoomService extends IService<RoomDto> {

    List<RoomDto> listByBuild(int buildId);
}

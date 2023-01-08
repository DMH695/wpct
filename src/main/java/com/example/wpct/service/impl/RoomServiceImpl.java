package com.example.wpct.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.RoomDto;
import com.example.wpct.mapper.RoomMapper;
import com.example.wpct.service.RoomService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, RoomDto> implements RoomService {
    @Override
    public List<RoomDto> listByBuild(int buildId) {
        return query().eq("build_id",buildId).list();
    }
}

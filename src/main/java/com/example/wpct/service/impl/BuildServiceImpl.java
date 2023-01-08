package com.example.wpct.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.BuildDto;
import com.example.wpct.mapper.BuildMapper;
import com.example.wpct.service.BuildService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildServiceImpl extends ServiceImpl<BuildMapper, BuildDto> implements BuildService {
    @Override
    public List<BuildDto> listByVillage(int villageId) {
        return query().eq("village_id",villageId).list();
    }
}

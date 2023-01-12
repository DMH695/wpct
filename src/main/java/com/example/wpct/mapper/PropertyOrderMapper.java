package com.example.wpct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wpct.entity.PropertyOrderDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PropertyOrderMapper extends BaseMapper<PropertyOrderDto> {
    void updateStatus(int orderNo);
}

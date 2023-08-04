package com.example.wpct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wpct.entity.PropertyOrderDto;
import com.example.wpct.entity.SharedFeeOrderDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PropertyOrderMapper extends BaseMapper<PropertyOrderDto> {
    void updateStatus(long orderNo);
    void updateStatus1(long orderNo);
    void updateRid(long id,Integer rid);
    PropertyOrderDto getById(Long id);
}

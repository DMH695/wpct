package com.example.wpct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wpct.entity.SharedFeeOrderDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SharedFeeOrderMapper extends BaseMapper<SharedFeeOrderDto> {
    void updateDate(Long order_no,String date);
    void updateStatus(Long order_no);
    void updateRid(long id,Integer rid);
    SharedFeeOrderDto getById(Long id);
}

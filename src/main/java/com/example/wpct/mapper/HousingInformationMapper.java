package com.example.wpct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wpct.entity.HousingInformationDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HousingInformationMapper extends BaseMapper<HousingInformationDto> {
    void updateDate(int house_id);
    void investProperty(Double property_fee,int hid);
    void investShare(Double poolBanlance,int hid);
}

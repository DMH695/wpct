package com.example.wpct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wpct.entity.HousingInformationDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HousingInformationMapper extends BaseMapper<HousingInformationDto> {
    void updateDate(long house_id);
    void investProperty(Double property_fee,int hid);
    void investShare(Double poolBanlance,int hid);
    HousingInformationDto getByVbr(String villageName,String buildName,String roomNum);
    void updateBindCount(int hid,String count);
    void updatePropertyFee(long hid,double propertyFee);
    void updateSharedFee(long hid,double sharedFee);
}

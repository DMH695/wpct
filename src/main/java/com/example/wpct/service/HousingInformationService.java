package com.example.wpct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.vo.HousingInformationVo;

import java.util.List;

public interface HousingInformationService extends IService<HousingInformationDto> {

    List<HousingInformationDto> listByVo(HousingInformationVo vo);
}

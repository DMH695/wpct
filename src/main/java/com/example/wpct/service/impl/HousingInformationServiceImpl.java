package com.example.wpct.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.vo.HousingInformationVo;
import com.example.wpct.mapper.HousingInformationMapper;
import com.example.wpct.service.HousingInformationService;
import com.example.wpct.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HousingInformationServiceImpl extends ServiceImpl<HousingInformationMapper, HousingInformationDto> implements HousingInformationService {


    @Override
    public List<HousingInformationDto> listByVo(HousingInformationVo vo) {
        QueryChainWrapper<HousingInformationDto> condition = query()
                .like(StringUtils.isNotEmpty(vo.getVillageName()),"village_name", vo.getVillageName())
                .eq(StringUtils.isNotEmpty(vo.getBuildNumber()),"build_number", vo.getBuildNumber())
                .eq(StringUtils.isNotEmpty(vo.getHouseNo()),"house_no", vo.getHouseNo());
        return baseMapper.selectList(condition);
    }
}

package com.example.wpct.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.vo.HousingInformationVo;
import com.example.wpct.mapper.HousingInformationMapper;
import com.example.wpct.service.HousingInformationService;
import com.example.wpct.utils.StringUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HousingInformationServiceImpl extends ServiceImpl<HousingInformationMapper, HousingInformationDto> implements HousingInformationService {


    @Override
    public PageInfo<HousingInformationDto> listByVo(HousingInformationVo vo) {
        int pageNum = vo.getPageNum();
        int pageSize = vo.getPageSize();
        Page<HousingInformationDto> page = PageHelper.startPage(pageNum,pageSize);
        List<HousingInformationDto> res = query()
                .like(StringUtils.isNotEmpty(vo.getVillageName()),"village_name", vo.getVillageName())
                .eq(StringUtils.isNotEmpty(vo.getBuildNumber()),"build_number", vo.getBuildNumber())
                .eq(StringUtils.isNotEmpty(vo.getHouseNo()),"house_no", vo.getHouseNo()).list();

        return new PageInfo<>(res);
    }
}

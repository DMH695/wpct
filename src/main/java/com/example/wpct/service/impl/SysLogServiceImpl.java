package com.example.wpct.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.SysLogDto;
import com.example.wpct.entity.vo.LogVo;
import com.example.wpct.mapper.SysLogMapper;
import com.example.wpct.service.SysLogService;
import com.example.wpct.utils.ResultBody;
import com.example.wpct.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLogDto> implements SysLogService {

    @Override
    public ResultBody list(LogVo vo) {
        int pageNum  = vo.getPageNum();
        int pageSize = vo.getPageSize();
        PageHelper.startPage(pageNum,pageSize);
        List<SysLogDto> res = query()
                .eq(StringUtils.isNotEmpty(vo.getUsername()),"username",vo.getUsername())
                .ge(StringUtils.isNotEmpty(vo.getStartTime()),"create_date",vo.getStartTime())
                .le(StringUtils.isNotEmpty(vo.getEndTime()),"create_date",vo.getEndTime())
                .eq(StringUtils.isNotEmpty(vo.getType()),"type",vo.getType())
                .eq(StringUtils.isNotEmpty(vo.getModel()),"model",vo.getModel())
                .list();
        return ResultBody.ok(new PageInfo<>(res));
    }
}

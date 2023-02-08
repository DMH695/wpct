package com.example.wpct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wpct.entity.SysLogDto;
import com.example.wpct.entity.vo.LogVo;
import com.example.wpct.utils.ResultBody;

public interface SysLogService extends IService<SysLogDto> {

    ResultBody list(LogVo vo);
}

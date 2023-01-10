package com.example.wpct.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wpct.entity.SharedFeeDto;
import com.example.wpct.mapper.SharedFeeMapper;
import com.example.wpct.service.SharedFeeService;
import org.springframework.stereotype.Service;

@Service
public class SharedFeeServiceImpl extends ServiceImpl<SharedFeeMapper, SharedFeeDto> implements SharedFeeService {

}

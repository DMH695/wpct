package com.example.wpct.service.impl;

import com.example.wpct.mapper.SysUserMapper;
import com.example.wpct.entity.SysUser;
import com.example.wpct.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl implements SysUserService {
    @Autowired
    SysUserMapper sysUserMapper;

    @Override
    public SysUser getByUserName(String username) {
        return sysUserMapper.getByUserName(username);
    }
}

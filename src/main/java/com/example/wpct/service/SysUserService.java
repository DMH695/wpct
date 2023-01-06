package com.example.wpct.service;

import com.example.wpct.entity.SysUser;

public interface SysUserService {
    SysUser getByUserName(String username);
}

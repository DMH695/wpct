package com.example.wpct.service;

import com.example.wpct.dao.SysUserDao;
import com.example.wpct.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl implements SysUserService{
    @Autowired
    SysUserDao sysUserDao;

    @Override
    public SysUser getByUserName(String username) {
        return sysUserDao.getByUserName(username);
    }
}

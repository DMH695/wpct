package com.example.wpct.service;

import com.example.wpct.entity.SysUser;
import com.example.wpct.utils.page.PageRequest;
import com.example.wpct.utils.page.PageResult;

import java.util.List;

public interface SysUserService {
    SysUser getByUserName(String username);
    PageResult getAll(PageRequest pageRequest);
    void insert(SysUser sysUser);
    void update(SysUser sysUser);
    void delete(int id);
    SysUser getById(int id);
}

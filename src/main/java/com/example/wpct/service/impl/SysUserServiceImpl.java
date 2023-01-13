package com.example.wpct.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.wpct.mapper.SysUserMapper;
import com.example.wpct.entity.SysUser;
import com.example.wpct.service.SysUserService;
import com.example.wpct.utils.page.PageRequest;
import com.example.wpct.utils.page.PageResult;
import com.example.wpct.utils.page.PageUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SysUserServiceImpl implements SysUserService {
    @Autowired
    SysUserMapper sysUserMapper;

    public static Page page;

    @Override
    public SysUser getByUserName(String username) {
        return sysUserMapper.getByUserName(username);
    }

    @Override
    public PageResult getAll(PageRequest pageRequest) {
        return PageUtil.getPageResult(getPage(pageRequest),page);
    }

    private PageInfo<?> getPage(PageRequest pageRequest) {
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();
        //设置分页数据
        page = PageHelper.startPage(pageNum,pageSize);
        return new PageInfo<>(sysUserMapper.getAll());
    }


    @Override
    public void insert(SysUser sysUser) {
        sysUserMapper.insert(sysUser);
    }

    @Override
    public void update(SysUser sysUser) {
        sysUserMapper.update(sysUser);
    }

    @Override
    public void delete(int id) {
        sysUserMapper.delete(id);
    }

    @Override
    public SysUser getById(int id) {
        return sysUserMapper.getById(id);
    }
}

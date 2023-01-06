package com.example.wpct.dao;

import com.example.wpct.entity.SysUser;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserDao {
    SysUser getByUserName(String username);
}

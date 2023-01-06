package com.example.wpct.mapper;

import com.example.wpct.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper {
    SysUser getByUserName(String username);
}

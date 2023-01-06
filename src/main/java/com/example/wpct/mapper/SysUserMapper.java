package com.example.wpct.mapper;

import com.example.wpct.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper {
    SysUser getByUserName(String username);
    List<SysUser> getAll();
    void insert(@Param("user")SysUser user);
    void update(@Param("user")SysUser user);
    void delete(int id);
    SysUser getById(int id);
}

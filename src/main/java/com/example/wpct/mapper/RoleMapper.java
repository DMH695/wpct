package com.example.wpct.mapper;

import com.example.wpct.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {
    Role getById(int id);
    List<Role> all();
    void insert(String name,String permission);
    void update(int id,String name,String permission);
    void delete(int id);
}

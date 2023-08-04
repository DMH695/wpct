package com.example.wpct.mapper;

import com.example.wpct.entity.RollBack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RollBackMapper {
    void insert(@Param("r")RollBack rollBack);
    RollBack getById(int id);
}

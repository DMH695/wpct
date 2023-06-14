package com.example.wpct.mapper;

import com.example.wpct.entity.Refund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RefundMapper {
    void insert(@Param("r")Refund refund);
    List<Refund> all();
    void delete(int id);
}

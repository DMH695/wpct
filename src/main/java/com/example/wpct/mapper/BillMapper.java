package com.example.wpct.mapper;

import com.example.wpct.entity.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BillMapper {
    void insert(@Param("bill")Bill bill);
}

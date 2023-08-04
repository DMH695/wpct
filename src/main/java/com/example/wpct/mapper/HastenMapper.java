package com.example.wpct.mapper;

import com.example.wpct.entity.Hasten;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HastenMapper {
    int getCountByHid(int hid);
    List<Hasten> getByHid(int hid);
    void insert(@Param("h") Hasten hasten);
}

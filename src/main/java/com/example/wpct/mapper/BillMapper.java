package com.example.wpct.mapper;

import com.example.wpct.entity.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BillMapper {
    void insert(@Param("bill")Bill bill);
    List<Bill> getAll(String villageName,String buildName,String roomNum);
    void delete(int id);
    Bill selectById(int id);
    List<Bill> getByOpenid(String openid);
    List<Bill> getByWid(String out_trade_no);
    List<Bill> getOne(String openid,String villageName,String buildName,String roomNum);
    void updateStatus(String out_trade_no);
}

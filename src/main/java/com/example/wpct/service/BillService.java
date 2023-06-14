package com.example.wpct.service;

import com.example.wpct.entity.Bill;
import com.example.wpct.utils.ResultBody;
import com.example.wpct.utils.page.PageRequest;
import com.example.wpct.utils.page.PageResult;

import java.util.List;

public interface BillService {
    PageResult getAll(PageRequest pageRequest,String villageName,String buildName,String roomNum);
    void delete(int id);
    ResultBody getReceiptCertificate(int id);
    List<Bill> getByOpenid(String openid,String villageName,String buildName,String roomNum);
    List<Bill> getByWid(String out_trade_no);
}

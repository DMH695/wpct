package com.example.wpct.service;

import com.example.wpct.entity.RefundInfo;

import java.util.List;

public interface RefundInfoService {
    RefundInfo createRefundByOrderNo(String orderNo, String reason,Integer refundFee);

    void updateRefund(String content);

    List<RefundInfo> getNoRefundOrderByDuration(int minutes);
}

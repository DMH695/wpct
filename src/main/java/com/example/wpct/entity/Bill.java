package com.example.wpct.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bill {
    private int id;
    private Double pay;//缴交费用
    private String detail;//缴交详情
    private String openid;
    private String villageName;
    private String buildName;
    private String roomNum;
    private String type;//缴交方式
    private String location;//从哪里扣钱
    private String date;//缴交日期
}

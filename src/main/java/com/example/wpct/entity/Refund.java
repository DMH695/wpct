package com.example.wpct.entity;

import lombok.Data;

@Data
public class Refund {
    private int id;
    private String openid;
    private int hid;
    private double cost;
    private String date;
}

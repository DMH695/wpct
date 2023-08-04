package com.example.wpct.entity;

import lombok.Data;

@Data
public class RollBack {
    private int id;
    private String detail;
    private long paymentStatus;
    private int uid;
    private String time;
    private double cost;
}

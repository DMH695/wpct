package com.example.wpct.entity;

import lombok.Data;

@Data
public class Role {
    private int id;
    private String name;
    private String permission;
    //授权数据
    private String data;
}

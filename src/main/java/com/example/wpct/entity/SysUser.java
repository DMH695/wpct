package com.example.wpct.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysUser implements Serializable {
    private int id;
    private String username;
    private String name;
    private String password;
    private String role;
}

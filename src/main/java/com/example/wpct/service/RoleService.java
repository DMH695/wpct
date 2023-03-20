package com.example.wpct.service;

import com.example.wpct.entity.Role;

import java.util.List;

public interface RoleService {
    Role getById(int id);
    List<Role> all();
    void insert(String name,String permission);
    void update(int id,String name,String permission);
    void delete(int id);
    void authData(String villageNames,int role);
}

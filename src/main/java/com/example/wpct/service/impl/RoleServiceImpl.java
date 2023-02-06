package com.example.wpct.service.impl;

import com.example.wpct.entity.Role;
import com.example.wpct.mapper.RoleMapper;
import com.example.wpct.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    RoleMapper roleMapper;

    @Override
    public Role getById(int id) {
        return roleMapper.getById(id);
    }

    @Override
    public List<Role> all() {
        return roleMapper.all();
    }

    @Override
    public void insert(String name,String permission) {
        roleMapper.insert(name, permission);
    }

    @Override
    public void update(int id, String name, String permission) {
        roleMapper.update(id, name, permission);
    }

    @Override
    public void delete(int id) {
        roleMapper.delete(id);
    }
}

package com.example.wpct.service;

import com.example.wpct.entity.RollBack;

public interface RollBackService {
    void insert(RollBack rollBack);
    RollBack getById(int id);
}

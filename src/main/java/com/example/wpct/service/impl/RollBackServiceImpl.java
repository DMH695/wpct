package com.example.wpct.service.impl;

import com.example.wpct.entity.RollBack;
import com.example.wpct.mapper.RollBackMapper;
import com.example.wpct.service.RollBackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RollBackServiceImpl implements RollBackService {
    @Autowired
    RollBackMapper rollBackMapper;


    @Override
    public void insert(RollBack rollBack) {
        rollBackMapper.insert(rollBack);
    }

    @Override
    public RollBack getById(int id) {
        return rollBackMapper.getById(id);
    }
}

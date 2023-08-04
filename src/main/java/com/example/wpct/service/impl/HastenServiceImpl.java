package com.example.wpct.service.impl;

import com.example.wpct.entity.Hasten;
import com.example.wpct.mapper.HastenMapper;
import com.example.wpct.service.HastenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HastenServiceImpl implements HastenService {
    @Autowired
    HastenMapper hastenMapper;

    @Override
    public int getCountByHid(int hid) {
        return hastenMapper.getCountByHid(hid);
    }

    @Override
    public List<Hasten> getByHid(int hid) {
        return hastenMapper.getByHid(hid);
    }

    @Override
    public void insert(Hasten hasten) {
        hastenMapper.insert(hasten);
    }
}

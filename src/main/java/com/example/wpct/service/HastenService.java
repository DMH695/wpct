package com.example.wpct.service;

import com.example.wpct.entity.Hasten;

import java.util.List;

public interface HastenService {
    int getCountByHid(int hid);
    List<Hasten> getByHid(int hid);
    void insert(Hasten hasten);
}

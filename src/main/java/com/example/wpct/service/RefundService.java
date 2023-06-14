package com.example.wpct.service;

import com.example.wpct.utils.page.PageRequest;
import com.example.wpct.utils.page.PageResult;

import java.util.List;

public interface RefundService {
    PageResult getAll(PageRequest pageRequest);
    void delete(int id);
}

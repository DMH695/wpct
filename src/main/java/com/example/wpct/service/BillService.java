package com.example.wpct.service;

import com.example.wpct.utils.page.PageRequest;
import com.example.wpct.utils.page.PageResult;

public interface BillService {
    PageResult getAll(PageRequest pageRequest,String villageName,String buildName,String roomNum);
    void delete(int id);
}

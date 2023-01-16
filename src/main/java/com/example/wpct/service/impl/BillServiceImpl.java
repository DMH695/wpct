package com.example.wpct.service.impl;

import com.example.wpct.mapper.BillMapper;
import com.example.wpct.service.BillService;
import com.example.wpct.utils.page.PageRequest;
import com.example.wpct.utils.page.PageResult;
import com.example.wpct.utils.page.PageUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BillServiceImpl implements BillService {

    public static Page page;

    @Autowired
    BillMapper billMapper;

    @Override
    public PageResult getAll(PageRequest pageRequest, String villageName, String buildName, String roomNum) {
        return PageUtil.getPageResult(getPage(pageRequest,villageName,buildName,roomNum),page);

    }

    @Override
    public void delete(int id) {
        billMapper.delete(id);
    }

    private PageInfo<?> getPage(PageRequest pageRequest,String villageName,String buildName,String roomNum) {
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();
        //设置分页数据
        page = PageHelper.startPage(pageNum,pageSize);
        return new PageInfo<>(billMapper.getAll(villageName,buildName,roomNum));
    }
}

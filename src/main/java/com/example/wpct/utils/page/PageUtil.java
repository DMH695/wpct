package com.example.wpct.utils.page;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;

public class PageUtil {
    /**
     * 将分页信息封装到统一的接口
     * @param
     * @param pageInfo
     * @return
     */
    public static PageResult getPageResult(PageInfo<?> pageInfo,Page page){
        PageResult pageResult = new PageResult();
        pageResult.setPageNum(page.getPageNum());
        pageResult.setPageSize(page.getPageSize());
        pageResult.setTotalSize(page.getTotal());
        pageResult.setTotalPages(page.getPages());
        pageResult.setContent(pageInfo.getList());
        return pageResult;
    }
}

package com.example.wpct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wpct.entity.ExamineDto;
import com.example.wpct.utils.ResultBody;
import com.example.wpct.utils.page.PageResult;
import org.springframework.stereotype.Service;


/**
 * @Author ZXX
 * @InterfaceName ExamineService
 * @Description TODO
 * @DATE 2022/10/10 17:03
 */
@Service
public interface ExamineService extends IService<ExamineDto> {
    ResultBody addExamine(String openid,String examineContentString,int hid);
    PageResult listExamine(int pageNum, int pageSize);
    ResultBody examineHandle(Integer id,String resolveMsg);
    ResultBody userExamineList(String openid,int hid);

}

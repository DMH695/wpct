package com.example.wpct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wpct.entity.SharedFeeDto;
import com.example.wpct.entity.vo.SharedFeeVo;
import com.example.wpct.utils.ResultBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface SharedFeeService extends IService<SharedFeeDto> {

    ResultBody list(SharedFeeVo vo);

    void getTemplate(HttpServletResponse response);

    ResultBody importSharedFee(MultipartFile file);

}

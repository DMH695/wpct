package com.example.wpct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wpct.entity.SharedFeeOrderDto;
import com.example.wpct.entity.vo.SharedFeeOrderVo;
import com.example.wpct.utils.ResultBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface SharedFeeOrderService extends IService<SharedFeeOrderDto> {

    ResultBody insert(SharedFeeOrderDto dto);

    ResultBody list(SharedFeeOrderVo vo);

    void getTemplate(HttpServletResponse response);

    ResultBody importOrder(MultipartFile file);

    ResultBody executeDeduction();

    ResultBody updateByDto(SharedFeeOrderDto dto);
}

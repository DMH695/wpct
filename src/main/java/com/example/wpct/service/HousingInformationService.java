package com.example.wpct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.vo.HousingInformationVo;
import com.example.wpct.utils.ResultBody;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.util.List;

public interface HousingInformationService extends IService<HousingInformationDto> {

    PageInfo<HousingInformationDto> listByVo(HousingInformationVo vo);

    List<Long> getIdsByHouseInfo(String villageName, String buildNumber, String houseNo);

    void getTemplate(HttpServletResponse response);

    ResultBody importHousingInformation(MultipartFile file);

    HousingInformationDto getByVbr(String villageName,String buildName,String roomNum);

    ResultBody insert(HousingInformationDto dto);

    ResultBody updateByDto(HousingInformationDto dto);

}

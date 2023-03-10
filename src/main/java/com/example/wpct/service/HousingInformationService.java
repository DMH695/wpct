package com.example.wpct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.wpct.entity.BuildDto;
import com.example.wpct.entity.HousingInformationDto;
import com.example.wpct.entity.VillageDto;
import com.example.wpct.entity.vo.HousingInformationVo;
import com.example.wpct.utils.ResultBody;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;
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

    List<HousingInformationDto> listByVillage(VillageDto village);

    List<HousingInformationDto> listByBuild(BuildDto build);

    List<HousingInformationDto> listByVillageName(String villageName);

    List<HousingInformationDto> listByBuildNumber(String buildNumber);

    ResultBody deleteByWechat(String openId, Integer houseId);

    ResultBody getCostEstimate(Long hid);

    void updateBindCount(int hid,int count);

}

package com.example.wpct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wpct.entity.ExamineDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author ZXX
 * @InterfaceName ExamineMapper
 * @Description TODO
 * @DATE 2022/10/10 17:03
 */
@Mapper
public interface ExamineMapper extends BaseMapper<ExamineDto> {

    List<ExamineDto>  listExamine();
}

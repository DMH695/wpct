package com.example.wpct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wpct.entity.WechatUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author ZXX
 * @InterfaceName WechatUserMapper
 * @Description
 * @DATE 2022/10/1 17:15
 */
@Mapper
public interface WechatUserMapper extends BaseMapper<WechatUser> {
}

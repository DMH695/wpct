package com.example.wpct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wpct.entity.WechatUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author ZXX
 * @InterfaceName WechatUserMapper
 * @Description
 * @DATE 2022/10/1 17:15
 */
@Mapper
public interface WechatUserMapper extends BaseMapper<WechatUser> {
    List<WechatUser> getByOpenid(String openid);
    void bind(@Param("w")WechatUser wechatUser);
    WechatUser checkBind(String openid, int hid);
}

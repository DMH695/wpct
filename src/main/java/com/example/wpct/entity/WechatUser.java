package com.example.wpct.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("wechat_user")
public class WechatUser {

    /**
     * 主键id
     */
    private int id;

    /**
     * 微信用户昵称
     */
    @TableField("nickName")
    private String nickName;

    /**
     * 微信用户姓名
     */
    private String name;

    /**
     * 电话号码
     */
    private String telephone;

    /**
     * openid
     */
    private String openid;

    /**
     * village_name
     */
    @TableField(exist = false)
    private String villageName;

    /**
     * build_number
     */
    @TableField(exist = false)
    private String buildNumber;

    /**
     * 房号 house_no
     */
    @TableField(exist = false)
    private String houseNo;
    private String relation;

    private int hid;


}

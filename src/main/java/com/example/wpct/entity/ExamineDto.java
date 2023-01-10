package com.example.wpct.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;


@Data
@TableName("tb_examine")
public class ExamineDto implements Serializable {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * openid
     */
    private String openid;
    /**
     * 意见内容
     */
    private String examineContent;
    /**
     * 处理信息
     */
    private String resolveHandle;
    /**
     * 处理人姓名
     */
    private String uname;
    /**
     * 处理状态
     */
    private String approvalStatus;
    /**
     * 提交时间
     */
    private LocalDateTime commitTime;
    /**
     * 小区id (village_id)
     */
    private Integer vid;

    /**
     * 楼栋id (build_id)
     */
    private Integer bid;

    /**
     * 房号
     */
    private Integer rid;
    /**
     * 微信用户具体信息
     */
    @TableField(exist = false)
    private WechatUser wechatUser;
    /**
     * 楼栋
     */
    @TableField(exist = false)
    private BuildDto buildDto;
    /**
     * 小区
     */
    @TableField(exist = false)
    private VillageDto villageDto;
    /**
     * 房号
     */
    @TableField(exist = false)
    private RoomDto roomDto;
}

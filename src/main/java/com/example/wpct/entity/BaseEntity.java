package com.example.wpct.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BaseEntity implements Serializable {

    //定义主键策略：跟随数据库的主键自增
    @TableId(value = "id", type = IdType.AUTO)
    private String id; //主键

    private Date createTime;//创建时间

    private Date updateTime;//更新时间
}


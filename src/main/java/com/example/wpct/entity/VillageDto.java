package com.example.wpct.entity;

import java.io.Serializable;
import java.math.BigInteger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.alibaba.fastjson.annotation.JSONField;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("village")
public class VillageDto implements Serializable {

	private static final long serialVersionUID = 1234229491564789248L;



	/**
	 * id
	 */
	@JSONField(name ="id")
	@TableId(value = "id", type = IdType.AUTO)
	private long id;


	/**
	 * 小区名
	 */
	@JSONField(name ="name")
	private String name;

}

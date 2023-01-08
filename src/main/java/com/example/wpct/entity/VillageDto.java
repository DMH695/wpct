package com.example.wpct.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.alibaba.fastjson.annotation.JSONField;


@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("village")
public class VillageDto implements Serializable {

	private static final long serialVersionUID = 1234229491564789248L;



	/**
	 * id
	 */
	@JSONField(name ="id")
	private long id;


	/**
	 * 小区名
	 */
	@JSONField(name ="name")
	private String name;

}

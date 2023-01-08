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
@TableName("build")
public class BuildDto implements Serializable {

	private static final long serialVersionUID = 1805213355100658688L;



	/**
	 * id
	 */
	@JSONField(name ="id")
	private long id;


	/**
	 * 小区id
	 */
	@JSONField(name ="villageId")
	private long villageId;


	/**
	 * 楼号
	 */
	@JSONField(name ="name")
	private String name;

}

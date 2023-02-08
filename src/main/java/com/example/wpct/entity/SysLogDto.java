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
@TableName("sys_log")
public class SysLogDto implements Serializable {

	private static final long serialVersionUID = 1113470966894577920L;



	/**
	 * id
	 */
	@JSONField(name ="id")
	private long id;


	/**
	 * 用户名
	 */
	@JSONField(name ="username")
	private String username;


	/**
	 * 方法名
	 */
	@JSONField(name ="method")
	private String method;


	/**
	 * 参数
	 */
	@JSONField(name ="params")
	private String params;


	/**
	 * ip地址
	 */
	@JSONField(name ="ip")
	private String ip;


	/**
	 * 创建时间
	 */
	@JSONField(name ="createDate")
	private java.sql.Timestamp createDate;


	/**
	 * 类型
	 */
	@JSONField(name ="type")
	private String type;


	/**
	 * 模块
	 */
	@JSONField(name ="model")
	private String model;


	/**
	 * 操作结果
	 */
	@JSONField(name ="result")
	private String result;


	/**
	 * 描述
	 */
	@JSONField(name ="description")
	private String description;


	/**
	 * 请求url
	 */
	@JSONField(name ="url")
	private String url;

}

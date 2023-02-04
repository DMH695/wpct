package com.example.wpct.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.alibaba.fastjson.annotation.JSONField;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDto implements Serializable {

	private static final long serialVersionUID = 1723420566791661568L;



	/**
	 * 小区
	 */
	@JSONField(name ="villageName")
	private String villageName;

	/**
	 * 楼栋号
	 */
	@JSONField(name ="buildNumber")
	private String buildNumber;


	/**
	 * 开始日期
	 */
	@JSONField(name ="startDate")
	private java.sql.Date startDate;


	/**
	 * 截止日期
	 */
	@JSONField(name ="endDate")
	private java.sql.Date endDate;


	/**
	 * 收账率
	 */
	@JSONField(name ="rate")
	private double rate;


	/**
	 * 应收账款
	 */
	@JSONField(name ="receivable")
	private double receivable;


	/**
	 * 实收账款
	 */
	@JSONField(name ="received")
	private double received;

}

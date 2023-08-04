package com.example.wpct.entity;

import java.io.Serializable;

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
	 * 楼栋号
	 */
	@JSONField(name ="buildNumber")
	private String buildNumber;


	/**
	 * 小区
	 */
	@JSONField(name ="houseName")
	private String houseName;


	/**
	 * 姓名
	 */
	@JSONField(name = "name")
	private String name;


	/**
	 * 电话
	 */
	@JSONField(name = "phone")
	private String phone;


	/**
	 * 物业费欠费余额
	 */
	@JSONField(name = "propertyOutstanding")
	private double propertyOutstanding;


	/**
	 * 公摊费欠费余额
	 */
	@JSONField(name = "sharedOutstanding")
	private double sharedOutstanding;


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


	/**
	 * 截止日期
	 */
	@JSONField(name = "deadline")
	private String deadline;

	/**
	 * 物业费欠费月数
	 */
	@JSONField(name = "pCount")
	private int pCount;

	/**
	 * 公摊费欠费月数
	 */
	@JSONField(name = "sCount")
	private int sCount;
}

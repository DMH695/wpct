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
public class HousingInformationDto implements Serializable {

	private static final long serialVersionUID = 1682089260817101312L;


	@JSONField(name ="id")
	private long id;


	/**
	 * 小区
	 */
	@JSONField(name ="villageName")
	private String villageName;


	/**
	 * 楼号
	 */
	@JSONField(name ="buildNumber")
	private String buildNumber;


	/**
	 * 房号
	 */
	@JSONField(name ="houseNo")
	private String houseNo;


	/**
	 * 登记姓名
	 */
	@JSONField(name ="name")
	private String name;


	/**
	 * 登记的电话号码
	 */
	@JSONField(name ="phone")
	private String phone;


	/**
	 * 户口所在地
	 */
	@JSONField(name ="resident")
	private String resident;


	/**
	 * 房屋类型
	 */
	@JSONField(name ="houseType")
	private String houseType;


	/**
	 * 车辆类型
	 */
	@JSONField(name ="carType")
	private String carType;


	/**
	 * 与房屋的关系
	 */
	@JSONField(name ="relation")
	private String relation;


	/**
	 * 符合条件的人数
	 */
	@JSONField(name ="conditionNumber")
	private long conditionNumber;


	/**
	 * 低保人数
	 */
	@JSONField(name ="lowNumber")
	private long lowNumber;


	/**
	 * 租金
	 */
	@JSONField(name ="rent")
	private double rent;


	/**
	 * 面积
	 */
	@JSONField(name ="area")
	private double area;


	/**
	 * 核准面积
	 */
	@JSONField(name ="trueArea")
	private double trueArea;


	/**
	 * 超出面积
	 */
	@JSONField(name ="exceedArea")
	private double exceedArea;


	/**
	 * 超出面积单价
	 */
	@JSONField(name ="exceedAreaUnitPrice")
	private double exceedAreaUnitPrice;


	/**
	 * 面积单价
	 */
	@JSONField(name ="areaUnitPrice")
	private double areaUnitPrice;


	/**
	 * 停车费
	 */
	@JSONField(name ="carFee")
	private double carFee;


	/**
	 * 其他费用
	 */
	@JSONField(name ="otherFee")
	private double otherFee;


	/**
	 * 停车位号1
	 */
	@JSONField(name ="stopNumberOne")
	private String stopNumberOne;


	/**
	 * 停车位号2
	 */
	@JSONField(name ="stopNumberTwo")
	private String stopNumberTwo;


	/**
	 * 收回不符条件疫情减免金额
	 */
	@JSONField(name ="recycleFee")
	private double recycleFee;


	/**
	 * 收回不符合条件租金
	 */
	@JSONField(name ="recycleRent")
	private double recycleRent;


	/**
	 * 应收应退租金
	 */
	@JSONField(name ="calculateRent")
	private double calculateRent;


	/**
	 * 应收应退物业费
	 */
	@JSONField(name ="calculateFee")
	private double calculateFee;


	/**
	 * 优惠
	 */
	@JSONField(name ="discount")
	private double discount;


	/**
	 * 备注
	 */
	@JSONField(name ="remarks")
	private String remarks;


	/**
	 * 更新时间
	 */
	@JSONField(name ="updated")
	private java.sql.Timestamp updated;


	/**
	 * 公摊费余额
	 */
	@JSONField(name ="poolBalance")
	private double poolBalance;


	/**
	 * 物业费余额
	 */
	@JSONField(name ="propertyFee")
	private double propertyFee;


	/**
	 * 更新用户
	 */
	@JSONField(name ="updateUser")
	private String updateUser;


	/**
	 * 绑定微信用户
	 */
	@JSONField(name ="bindWechatUser")
	private String bindWechatUser;


	/**
	 * 缴交日期
	 */
	@JSONField(name ="dueDate")
	private java.sql.Timestamp dueDate;

}

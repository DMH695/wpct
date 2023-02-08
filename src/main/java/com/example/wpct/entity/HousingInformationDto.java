package com.example.wpct.entity;

import java.io.Serializable;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.alibaba.fastjson.annotation.JSONField;

import javax.validation.constraints.Min;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("housing_information")
public class HousingInformationDto implements Serializable {

	private static final long serialVersionUID = 1682089260817101312L;


	@JSONField(name ="id")
	@ExcelIgnore
	private long id;


	/**
	 * 小区
	 */
	@JSONField(name ="villageName")
	@ExcelProperty("小区名")
	private String villageName;


	/**
	 * 楼号
	 */
	@JSONField(name ="buildNumber")
	@ExcelProperty("楼号")
	private String buildNumber;


	/**
	 * 房号
	 */
	@JSONField(name ="houseNo")
	@ExcelProperty("房号")
	private String houseNo;


	/**
	 * 登记姓名
	 */
	@JSONField(name ="name")
	@ExcelProperty("登记人姓名")
	private String name;


	/**
	 * 登记的电话号码
	 */
	@JSONField(name ="phone")
	@ExcelProperty("登记人电话号码")
	private String phone;


	/**
	 * 户口所在地
	 */
	@JSONField(name ="resident")
	@ExcelProperty("户口所在地")
	private String resident;


	/**
	 * 房屋类型
	 */
	@JSONField(name ="houseType")
	@ExcelProperty("房屋类型")
	private String houseType;


	/**
	 * 车辆类型
	 */
	@JSONField(name ="carType")
	@ExcelProperty("车辆类型")
	private String carType;


	/**
	 * 与房屋的关系
	 */
	@JSONField(name ="relation")
	@ExcelProperty("登记人与房屋的关系")
	private String relation;


	/**
	 * 符合条件的人数
	 */
	@JSONField(name ="conditionNumber")
	@ExcelProperty("符合条件的人数")
	@Min(0)
	private long conditionNumber;


	/**
	 * 低保人数
	 */
	@JSONField(name ="lowNumber")
	@ExcelProperty("其中低保人数")
	@Min(0)
	private long lowNumber;


	/**
	 * 租金
	 */
	@JSONField(name ="rent")
	@ExcelProperty("租金")
	@Min(0)
	private double rent;


	/**
	 * 面积
	 */
	@JSONField(name ="area")
	@ExcelProperty("房屋面积")
	@Min(0)
	private double area;


	/**
	 * 核准面积
	 */
	@JSONField(name ="trueArea")
	@ExcelProperty("房屋核准面积")
	@Min(0)
	private double trueArea;


	/**
	 * 超出面积
	 */
	@JSONField(name ="exceedArea")
	@ExcelProperty("超出面积")
	@Min(0)
	private double exceedArea;


	/**
	 * 超出面积单价
	 */
	@JSONField(name ="exceedAreaUnitPrice")
	@ExcelProperty("超出面积单价")
	@Min(0)
	private double exceedAreaUnitPrice;


	/**
	 * 面积单价
	 */
	@JSONField(name ="areaUnitPrice")
	@ExcelProperty("面积单价")
	@Min(0)
	private double areaUnitPrice;


	/**
	 * 停车费
	 */
	@JSONField(name ="carFee")
	@ExcelProperty("停车费")
	@Min(0)
	private double carFee;


	/**
	 * 其他费用
	 */
	@JSONField(name ="otherFee")
	@ExcelProperty("其他费用")
	@Min(0)
	private double otherFee;


	/**
	 * 停车位号1
	 */
	@JSONField(name ="stopNumberOne")
	@ExcelProperty("停车位号1")
	private String stopNumberOne;


	/**
	 * 停车位号2
	 */
	@JSONField(name ="stopNumberTwo")
	@ExcelProperty("停车位号2")
	private String stopNumberTwo;


	/**
	 * 收回不符条件疫情减免金额
	 */
	@JSONField(name ="recycleFee")
	@ExcelProperty("收回不符条件疫情减免金额")
	@Min(0)
	private double recycleFee;


	/**
	 * 收回不符合条件租金
	 */
	@JSONField(name ="recycleRent")
	@ExcelProperty("收回不符合条件租金")
	@Min(0)
	private double recycleRent;


	/**
	 * 应收应退租金
	 */
	@JSONField(name ="calculateRent")
	@ExcelProperty("应收应退租金")
	@Min(0)
	private double calculateRent;


	/**
	 * 应收应退物业费
	 */
	@JSONField(name ="calculateFee")
	@ExcelProperty("应收应退物业费")
	@Min(0)
	private double calculateFee;


	/**
	 * 优惠
	 */
	@JSONField(name ="discount")
	@ExcelProperty("优惠")
	@Min(0)
	private double discount;


	/**
	 * 备注
	 */
	@JSONField(name ="remarks")
	@ExcelProperty("备注")
	private String remarks;


	/**
	 * 更新时间
	 */
	@JSONField(name ="updated")
	@ExcelIgnore
	private String updated;


	/**
	 * 公摊费余额
	 */
	@JSONField(name ="poolBalance")
//	@ExcelProperty("公摊费余额")
	@ExcelIgnore
	private double poolBalance;


	/**
	 * 物业费余额
	 */
	@JSONField(name ="propertyFee")
//	@ExcelProperty("物业费余额")
	@ExcelIgnore
	private double propertyFee;


	/**
	 * 更新用户
	 */
	@JSONField(name ="updateUser")
	@ExcelIgnore
	private String updateUser;


	/**
	 * 绑定微信用户
	 */
	@JSONField(name ="bindWechatUser")
//	@ExcelProperty("微信绑定用户")
	@ExcelIgnore
	private String bindWechatUser;
	/**
	 * 是否绑定微信用户
	 */
	@ExcelIgnore
	@TableField(exist = false)
	private boolean isBind;

	/**
	 * 缴交日期
	 */
	@JSONField(name ="dueDate")
//	@ExcelProperty("缴交日期")
	@ExcelIgnore
	private String dueDate;

}

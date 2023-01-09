package com.example.wpct.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import com.alibaba.fastjson.annotation.JSONField;


@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("property_order")
@Builder
@ToString
public class PropertyOrderDto implements Serializable {

	private static final long serialVersionUID = 1299656748189478400L;



	/**
	 * 订单编号
	 */
	@JSONField(name ="orderNo")
	private long orderNo;


	/**
	 * 房屋信息id
	 */
	@JSONField(name ="houseId")
	private long houseId;


	/**
	 * 缴交状态
	 */
	@JSONField(name ="paymentStatus")
	private long paymentStatus;


	/**
	 * 缴交费用
	 */
	@JSONField(name ="cost")
	private double cost;


	/**
	 * 费用详细，使用json记录
	 */
	@JSONField(name ="costDetail")
	private String costDetail;


	/**
	 * 开始日期
	 */
	@JSONField(name ="beginDate")
	private java.sql.Date beginDate;


	/**
	 * 结束日期
	 */
	@JSONField(name ="endDate")
	private java.sql.Date endDate;


	/**
	 * 生成订单时间或更新时间
	 */
	@JSONField(name ="updateTime")
	private java.sql.Timestamp updateTime;

}

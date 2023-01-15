package com.example.wpct.entity;

import java.io.Serializable;
import java.math.BigInteger;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
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
	@ApiModelProperty(value = "订单号", hidden = true)
	@TableId(value = "order_no")
	@JsonSerialize(using= ToStringSerializer.class)
	private Long orderNo;


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
	@ApiModelProperty(value = "费用详细，使用json记录")
	private String costDetail;


	/**
	 * 开始日期
	 */
	@JSONField(name ="beginDate")
	@ApiModelProperty(value = "开始日期，格式2020-04-27")
	private java.sql.Date beginDate;


	/**
	 * 结束日期
	 */
	@JSONField(name ="endDate")
	@ApiModelProperty(value = "结束日期，格式2020-04-27")
	private java.sql.Date endDate;


	/**
	 * 生成订单时间或更新时间
	 */
	@JSONField(name ="updateTime")
	@ApiModelProperty(value = "更新时间", readOnly = true, hidden = true)
	private String updateTime;

}

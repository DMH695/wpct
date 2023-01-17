package com.example.wpct.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
@TableName("shared_fee_order")
public class SharedFeeOrderDto implements Serializable {

	private static final long serialVersionUID = 1041148337230169984L;



	/**
	 * 订单号
	 */
	@JSONField(name ="orderNo")
	@ApiModelProperty(value = "订单号", hidden = true)
	@TableId(value = "order_no")
	@JsonSerialize(using= ToStringSerializer.class)
	private long orderNo;


	/**
	 * 房屋信息id
	 */
	@JSONField(name ="houseId")
	private long houseId;


	/**
	 * 缴交费用
	 */
	@JSONField(name ="cost")
	private double cost;


	/**
	 * 缴交时间
	 */
	@JSONField(name ="payTime")
	@ApiModelProperty(value = "缴交时间，格式2023-01-15 06:16")
	private String payTime;


	/**
	 * 缴交状态
	 */
	@JSONField(name ="paymentStatus")
	@ApiModelProperty(value = "缴交状态，0为未缴，1为已缴")
	private long paymentStatus;


	/**
	 * 费用详细,json
	 */
	@JSONField(name ="cost_detail")
	private String costDetail;


	/**
	 * 开始日期
	 */
	@JSONField(name ="beginDate")
	@ApiModelProperty(value = "开始日期，格式2020-04-27")
	private String beginDate;


	/**
	 * 结束日期
	 */
	@JSONField(name ="endDate")
	@ApiModelProperty(value = "结束日期，格式2020-04-27")
	private String endDate;


	/**
	 * 更新时间
	 */
	@JSONField(name ="updateDate")
	@ApiModelProperty(value = "更新时间", hidden = true)
	private String updateDate;


	/**
	 * 修改人
	 */
	@JSONField(name ="updateUser")
	@ApiModelProperty(value = "更新用户", hidden = true)
	private String updateUser;

	@ApiModelProperty(hidden = true)
	@TableField(exist = false)
	private String villageName;

	@ApiModelProperty(hidden = true)
	@TableField(exist = false)
	private String buildNumber;

	@ApiModelProperty(hidden = true)
	@TableField(exist = false)
	private String houseNo;

}

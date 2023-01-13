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
@TableName("shared_fee")
public class SharedFeeDto implements Serializable {

	private static final long serialVersionUID = 1385175786407876608L;



	/**
	 * id
	 */
	@JSONField(name ="id")
	@ApiModelProperty(value = "id", hidden = true)
	private long id;


	/**
	 * 房屋id
	 */
	@JSONField(name ="houseId")
	private long houseId;


	/**
	 * 电梯费
	 */
	@JSONField(name ="liftFee")
	private double liftFee;


	/**
	 * 电费
	 */
	@JSONField(name ="eleFee")
	private double eleFee;


	/**
	 * 水费
	 */
	@JSONField(name ="waterFee")
	private double waterFee;


	/**
	 * 更新日期
	 */
	@JSONField(name ="updateDate")
	@ApiModelProperty(value = "更新日期", hidden = true)
	private java.sql.Date updateDate;


	/**
	 * 修改人
	 */
	@JSONField(name ="updateUser")
	@ApiModelProperty(value = "更新用户", hidden = true)
	private String updateUser;

}

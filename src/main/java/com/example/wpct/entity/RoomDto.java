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
@TableName("room")
public class RoomDto implements Serializable {

	private static final long serialVersionUID = 1057037056588275072L;



	/**
	 * id
	 */
	@JSONField(name ="id")
	private long id;


	/**
	 * 楼栋id
	 */
	@JSONField(name ="buildId")
	private long buildId;


	/**
	 * 房号
	 */
	@JSONField(name ="name")
	private String name;

}

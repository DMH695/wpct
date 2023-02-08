package com.example.wpct.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogVo extends PageVo {
    private String username;
    private String startTime;
    private String endTime;
    private String type;
    private String model;
}

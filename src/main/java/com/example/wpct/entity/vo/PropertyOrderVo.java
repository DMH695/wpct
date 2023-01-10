package com.example.wpct.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.sql.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyOrderVo extends PageVo{

    private String beginDate;
    private String endDate;
    private String villageName;
    private String buildNumber;
    private String houseNo;

}

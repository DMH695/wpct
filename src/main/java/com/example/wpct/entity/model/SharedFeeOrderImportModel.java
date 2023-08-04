package com.example.wpct.entity.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SharedFeeOrderImportModel {

    @ExcelProperty("小区名")
    private String villageName;
    @ExcelProperty("电梯费")
    private double liftFee;
    @ExcelProperty("电费")
    private double eleFee;
    @ExcelProperty("其它费用")
    private double waterFee;
    @ExcelProperty("开始日期")
    private String beginDate;
    @ExcelProperty("结束日期")
    private String endDate;
}

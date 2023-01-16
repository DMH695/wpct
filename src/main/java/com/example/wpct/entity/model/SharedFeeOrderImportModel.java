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
    @ExcelProperty("楼号")
    private String buildNumber;
    @ExcelProperty("房号")
    private String houseNo;
    @ExcelProperty("电梯费")
    private double liftFee;
    @ExcelProperty("电费")
    private double eleFee;
    @ExcelProperty("水费")
    private double waterFee;
    @ExcelProperty("缴交状态")
    private Boolean paymentStatus;
    @ExcelProperty("缴交时间，若未缴交则为空")
    private String payTime;
    @ExcelProperty("开始日期")
    private String beginDate;
    @ExcelProperty("结束日期")
    private String endDate;
}

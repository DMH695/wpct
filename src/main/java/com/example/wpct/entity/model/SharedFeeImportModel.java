package com.example.wpct.entity.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.sun.istack.NotNull;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SharedFeeImportModel {
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

}

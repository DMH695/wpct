package com.example.wpct.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SharedFeeOrderVo extends PageVo{
    private String villageName;
    private String buildNumber;
    private String houseNo;
    @ApiModelProperty(value = "缴交状态，0未缴，1已缴")
    private Boolean paymentStatus;
    @ApiModelProperty(value = "缴交时间左区间，格式2023-01-15")
    private String pay_time_begin;
    @ApiModelProperty(value = "缴交时间右区间，格式2023-01-15")
    private String pay_time_end;

    @ApiModelProperty(value = "审批状态：待审批、已审批")
    private String check;
}

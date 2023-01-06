package com.example.wpct.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HousingInformationVo extends PageVo {
    private String villageName;
    private String buildNumber;
    private String houseNo;
}

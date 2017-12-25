package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 计息方式
 * Created by pengyueyang on 2016/12/12.
 */
@Getter
@AllArgsConstructor
public enum LoanLimitType {

    FIX_AMOUNT("3700","固定费用"),
    FIX_RATE("3701","固定费率");

    private String code;
    private String description;
}

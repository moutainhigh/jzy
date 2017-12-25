package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 还款时间类型
 * Created by pengyueyang on 2016/12/12.
 */
@Getter
@AllArgsConstructor
public enum LoanRepayDateType {

    REPAY_PRE("3600","期初收息"),
    REPAY_SUF("3601","期末收息");

    private String code;
    private String description;

    public static boolean isInLoanRepayDateType(String LoanRepayDateStr) {
        for (LoanRepayDateType loanRepayDateType:LoanRepayDateType.values()) {
            if (loanRepayDateType.name().equals(LoanRepayDateStr)) {
                return true;
            }
        }
        return false;
    }

}

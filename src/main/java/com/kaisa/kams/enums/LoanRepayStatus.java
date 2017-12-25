package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by pengyueyang on 2016/12/1.
 * 借贷还款状态
 */
@Getter
@AllArgsConstructor
public enum LoanRepayStatus {


    LOANED ("3400","还款中"),
    CLEARED("3401","已还清"),
    OVERDUE("3402","已逾期"),
    OVERDUE_CLEARED("3403","逾期还清"),
    AHEAD_CLEARED("3404","提前还清");

    private String code;
    private String description;

    public static boolean loanRepayIsCleared(LoanRepayStatus status) {
        if (LoanRepayStatus.CLEARED.equals(status)) {
            return true;
        }
        if (LoanRepayStatus.OVERDUE_CLEARED.equals(status)) {
            return true;
        }
        if (LoanRepayStatus.AHEAD_CLEARED.equals(status)) {
            return true;
        }
        return false;
    }
}

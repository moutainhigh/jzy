package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengyueyang on 2016/11/29.
 * 还款方式枚举
 */
@AllArgsConstructor
@Getter
public enum LoanRepayMethod {

    INTEREST_MONTHS("3300","先息后本（按月）"),
    INTEREST_DAYS("3301","先息后本（按天）"),
    EQUAL_INSTALLMENT("3302","等额本息"),
    BULLET_REPAYMENT("3303","一次性还本付息"),
    INTEREST_SEASONS("3304","按季付息，到期还本"),
    ALL("3399","全部");

    private String code;
    private String description;

    public String toJson() {
        return String.format("{\"code\":\"%s\",\"description\":\"%s\"}", code, description);
    }

    public static boolean isInLoanRepayMethod(String loanRepayMethodStr) {
        for (LoanRepayMethod loanRepayMethod:LoanRepayMethod.values()) {
            if (loanRepayMethod.name().equals(loanRepayMethodStr)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getLoanRepayMethod(){
        List<String> list=new ArrayList<>();
        for (LoanRepayMethod loanRepayMethod:LoanRepayMethod.values()) {
            list.add(loanRepayMethod.name());
        }
        return list;
    }
}

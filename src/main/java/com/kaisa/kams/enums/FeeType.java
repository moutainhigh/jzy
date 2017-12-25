package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by pengyueyang on 2016/11/29.
 * 费用类型枚举
 */
@AllArgsConstructor
@Getter
public enum FeeType {

    SERVICE_FEE("2000","借款服务费"),
    GUARANTEE_FEE("2001","借款担保费"),
    MANAGE_FEE("2002","资金管理费"),
    OVERDUE_FEE("2003", "逾期罚息"),
    PREPAYMENT_FEE("2004", "提前结清罚息"),
    PREPAYMENT_FEE_RATE("2005", "一次性服务费");

    private String code;
    private String description;
}

package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by yueyang on 2016/11/30.
 * 费用收取方式
 */
@Getter
@AllArgsConstructor
public enum FeeChargeType {

    FIXED_AMOUNT("2200","固定金额"),
    LOAN_AMOUNT_RATE("2201","贷款本金*比例"),
    REMAIN_PRINCIPAL_RATE("2202","剩余本金*比例"),
    OVERDUE_PRINCIPAL_RATE("2203","逾期本金*比例"),
    OVERDUE_REPAYMENT_RATE("2204","逾期本息*比例"),
    LOAN_REQUEST_INPUT("2205","前端录单输入");

    private String code;
    private String description;
}

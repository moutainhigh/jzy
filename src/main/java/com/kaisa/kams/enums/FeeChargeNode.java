package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by pengyueyang on 2016/12/12.
 * 费用收取节点
 */
@Getter
@AllArgsConstructor
public enum FeeChargeNode {

    LOAN_NODE("2600", "放款时收取"),
    REPAY_NODE("2601", "还款时收取");

    private String code;
    private String description;
}

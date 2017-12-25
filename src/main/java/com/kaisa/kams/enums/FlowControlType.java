package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程控制类型
 * Created by weid on 2016/12/6.
 */
@AllArgsConstructor
@Getter
public enum FlowControlType {

    BUSINESS_CONTROL("Y","业务审批"),
    RISK_CONTROL("F","风控审批"),
    FINANCE_CONTROL("C","财务审批"),

    M_BUSINESS_CONTROL("Y","业务模块"),
    M_RISK_CONTROL("F","风控模块"),
    M_FINANCE_CONTROL("C","财务模块"),
    M_SENIOR_EXECUTIVE("G","高管模块"),
    M_LOAN_PUSH_CONTROL("P","推单模块");

    private String code;
    private String description;
}

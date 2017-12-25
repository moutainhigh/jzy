package com.kaisa.kams.enums.push;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by pengyueyang on 2016/12/28.
 * 推单订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum LoanPushOrderStatus {

    EDIT("2600","编辑"),
    APPROVAL("2601","审批中"),
    REJECTED("2602","审批拒绝"),
    PUSHED("2603","已推送"),
    PLATFORM_REJECTED("2604","平台审批拒绝"),
    SCHEDULED("2605","已排期"),
    FUNDING("2606","筹款中"),
    FULL_FUNDED("2600","已满标"),
    LOANED("2601","还款中"),
    OVERDUE("2603","已逾期"),
    CLEARED("2600","已还清");

    private String code;
    private String description;
}

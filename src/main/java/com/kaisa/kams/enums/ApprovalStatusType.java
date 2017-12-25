package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by zhouchuang on 2017/9/15.
 */
@Getter
@AllArgsConstructor
public enum ApprovalStatusType {
    IN_EDIT("001","编辑中"),
    IN_APPROVAL("002","审批中"),
    APPROVED("003","已审批"),
    REJECT("004","已拒绝"),
    CANCEL("005","已取消");

    private String code;
    private String description;

}

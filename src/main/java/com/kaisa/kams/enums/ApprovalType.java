package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by weid on 2016/12/15.
 */

@Getter
@AllArgsConstructor
public enum ApprovalType {

    Y("01","业务审批"),
    F("02","风控审批"),
    C("03","财务审批"),
    G("04","高管审批");

    private String code;
    private String description;

    public static ApprovalType getApprovalTypeByName(String name) {
        for (ApprovalType approvalType : ApprovalType.values()) {
            if(approvalType.name().equals(name)){
                return approvalType;
            }
        }
        return null;
    }

    public static boolean contained(String taskKey){
        for (ApprovalType approvalType : ApprovalType.values()) {
            if(taskKey.contains(approvalType.name())){
                return true;
            }
        }
        return false;
    }

}

package com.kaisa.kams.enums;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by weid on 2016/12/15.
 */

@Getter
@AllArgsConstructor
public enum ApprovalCode {

    AGREE("01","同意"),
    BACKPRE("02","退回上一步"),
    BACKBEGIN("03","退回"),
    DISAGREE("04","拒绝");

    private String code;
    private String description;

    public static boolean isContainCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return false;
        }
        for (ApprovalCode approvalCode : ApprovalCode.values()) {
            if (approvalCode.name().equals(code)) {
                return true;
            }
        }
        return false;
    }
}

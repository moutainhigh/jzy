package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lw on 2017/11/6.
 * 贷款形式
 */
@Getter
@AllArgsConstructor
public enum LoanForm {

    NORMAL ("10001","正常"),
    CONCERN ("10002","关注"),
    SECONDARY ("10001","次级"),
    SUSPICIOUS ("10002","可疑"),
    LOSS ("10002","损失");

    private String code;
    private String description;
}

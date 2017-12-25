package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lw on 2017/8/29.
 * 金额类型
 */
@Getter
@AllArgsConstructor
public enum AmountType {

    LOAN_AMOUNT("AT001","借款金额"),
    INTERMEDIAR_FEE("AT002","居间费");

    private String code;
    private String description;
}

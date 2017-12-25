package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by yueyang on 2016/11/30.
 * 借款人证件类型
 */
@Getter
@AllArgsConstructor
public enum LoanerCertifType {

    ID ("3100","身份证"),
    DRIVE ("3101","驾驶证"),
    BUSINESS_LICENSE ("3102","营业执照"),
    PASS_ID("3103","港澳台证件号");

    private String code;
    private String description;
}

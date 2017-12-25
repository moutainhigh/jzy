package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by pengyueyang on 2016/12/5.
 * 放款主体类型
 */
@Getter
@AllArgsConstructor
public enum LoanSubjectType {

    ENTERPRISE("3500","企业"),
    PERSONAL("3501","个人");

    private String code;
    private String description;
}

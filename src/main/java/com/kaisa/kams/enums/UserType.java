package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by luoyj on 2016/12/29.
 * 用户类型
 */
@Getter
@AllArgsConstructor
public enum UserType {

    ORDINARY_USER("10001","普通用户"),
    BUSINESS_USER("10002","业务用户");

    private String code;
    private String description;
}

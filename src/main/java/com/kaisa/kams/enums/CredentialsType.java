package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by luoyj on 2016/12/13.
 * 证件类型
 */
@Getter
@AllArgsConstructor
public enum CredentialsType {

    SFZ("ZJ001","身份证");

    private String code;
    private String description;
}

package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by yueyang on 2016/11/30.
 * 系统用户证件类型
 */
@Getter
@AllArgsConstructor
public enum CertificateType {

    ID ("1600","身份证");

    private String code;
    private String description;
}

package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lw on 2017/11/02.
 * 公司类型
 */
@Getter
@AllArgsConstructor
public enum CompanyType {

    CREDITCOMPANY("51000","授信公司"),
    SUBCOMPANY("51001","子公司");


    private String code;
    private String description;
}

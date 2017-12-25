package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by pengyueyang on 2016/11/30.
 * 产品担保类型
 */
@Getter
@AllArgsConstructor
public enum MortgageType {

    MAXMORTGAGE("1700","最高额抵押"),
    GENMORTGAGE("1701","一般抵押");

    private String code;
    private String description;

}

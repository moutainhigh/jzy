package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lw on 2016/12/15.
 * 资信等级枚举
 */
@Getter
@AllArgsConstructor
public enum CreditRating {

    A("14001","AAA"),
    B("14002","AA"),
    C("14003","A");

    private String code;
    private String description;

}

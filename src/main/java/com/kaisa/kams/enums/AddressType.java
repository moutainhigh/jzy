package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lw on 2017/9/15.
 * 房产解押地类型
 */
@Getter
@AllArgsConstructor
public enum AddressType {

    SZMORTGAGE("1800","深圳房产解押"),
    GZMORTGAGE("1801","广州房产解押");

    private String code;
    private String description;

}

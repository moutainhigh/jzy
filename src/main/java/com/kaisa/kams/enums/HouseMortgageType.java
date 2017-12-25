package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by zhouchuang on 2017/9/15.
 */
@Getter
@AllArgsConstructor
public enum HouseMortgageType {
    SHENZHEN("001","深圳"),
    GUANGZHOU("002","广州");

    private String code;
    private String description;

}

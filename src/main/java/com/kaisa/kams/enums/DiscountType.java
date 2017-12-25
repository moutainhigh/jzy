package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by luoyj on 2016/12/13.
 * 贴现类型
 */
@Getter
@AllArgsConstructor
public enum DiscountType {

    DISCOUNT_PEOPLE("TX001","贴现人"),
    DISCOUNT_COMPANY("TX002","贴现企业");

    private String code;
    private String description;
}

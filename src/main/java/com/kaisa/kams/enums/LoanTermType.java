package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author  by pengyueyang on 2016/11/29.
 * 借款期限类型
 */
@AllArgsConstructor
@Getter
public enum LoanTermType {

    YEAS("3200","按年计算"),
    MOTHS("3201","按月计算"),
    DAYS("3202","按天计算"),
    FIXED_DATE("3203", "固定时间"),
    SEASONS("3204","按季计算");

    private String code;
    private String description;

    public static  String getTermStr(LoanTermType type, String term) {
        if (null == type || null == term) {
            return "";
        }
        switch (type){
            case YEAS:
                return term+"年";
            case DAYS:
                return term+"天";
            case MOTHS:
                return term+"个月";
            case FIXED_DATE:
                return "至"+term;
            case SEASONS:
                return term+"季";
            default:
                return "";
        }
    }
}

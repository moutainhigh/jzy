package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by pengyueyang on 2016/11/30.
 * 产品担保类型
 */
@Getter
@AllArgsConstructor
public enum GuarantyType {

    MORTGAGE("1100","抵押"),
    PLEDGE("1101","质押"),
    GUARANTY("1102","保证"),
    COUNTER_GUARANTY("1103","反担保"),
    NO_GUARANTY("1104","无担保");

    private String code;
    private String description;

    public static String getDescByEnumName(String name) {
        for(GuarantyType guarantyType : GuarantyType.values()){
            if ( guarantyType.name().equals(name)){
                return guarantyType.getDescription();
            }
        }
        return "找不到描述";
    }
}

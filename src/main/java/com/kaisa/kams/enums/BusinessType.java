package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by 彭岳阳 on 2016/11/30.
 * 业务类型
 */
@Getter
@AllArgsConstructor
public enum BusinessType {

    PUBLIC("1200","对公业务"),
    PRIVATE("1201","对私业务");

    private String code;
    private String description;

    public static String getDescByEnumName(String name) {
        for(BusinessType businessType : BusinessType.values()){
            if ( businessType.name().equals(name)){
                return businessType.getDescription();
            }
        }
        return "找不到描述";
    }
}

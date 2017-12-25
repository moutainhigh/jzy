package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by 彭岳阳 on 2016/11/30.
 * 业务条线
 */
@Getter
@AllArgsConstructor
public enum BusinessLine{

    HOUSE_LOAN("F","房贷"),
    CAR_LOAN("C","车贷"),
    CREDIT_LOAN("X","信贷"),
    PUBLIC_LOAN("G","对公"),
    CHANNEL_LOAN("Q","渠道"),
    GROUP_LOAN("J","集团");

    private String code;
    private String description;


    public static  String getCode(String businessLine){

        if (StringUtils.isEmpty(businessLine)){
            return null;
        }
        for (BusinessLine b:BusinessLine.values()) {
            if (businessLine.equals(b.toString())){
                return b.getCode();
            }
        }
        return  null;
    }
    public static  String getDescription(String businessLine){

        if (StringUtils.isEmpty(businessLine)){
            return null;
        }
        for (BusinessLine b:BusinessLine.values()) {
            if (businessLine.equals(b.toString())){
                return b.getDescription();
            }
        }
        return  null;
    }
}

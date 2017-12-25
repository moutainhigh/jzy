package com.kaisa.kams.enums;

import org.apache.commons.lang.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Created by luoyj on 2016/12/13.
 * 产品影像资料类型
 */
@Getter
@AllArgsConstructor
public enum ProductMediaItemType {

    BUSINESS("Y","业务影像资料"),
    RISK("F","风控影像资料"),
    FINANCE("C","财务影像资料"),
    POST_LOAN("D","贷后影像资料"),
    BILL("B","票据影像资料");


    private String code;
    private String description;

    public static ProductMediaItemType getEnum(String str){
        if (StringUtils.isNotEmpty(str)) {
            for (ProductMediaItemType type:ProductMediaItemType.values()) {
                if (str.equals(type.toString())){
                    return type;
                }
            }
        }
        return null;
    }

}

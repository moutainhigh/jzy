package com.kaisa.kams.enums.push;

import org.apache.commons.lang.StringUtils;
import org.nutz.json.Json;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 项目还款方式枚举
 * Created by pengyueyang on 2017/11/2.
 */
@Getter
@AllArgsConstructor
public enum PushRepayMethodType {

    BULLET_REPAYMENT(1, "一次性还款"),
    INTEREST(2, "先息后本"),
    EQUAL_INSTALLMENT(4, "等额本息"),
    EQUALITY(8, "等额本金");

    private Integer code;
    private String description;

    public static String getDescriptionByName(String name) {
        if (containName(name)) {
            return PushRepayMethodType.valueOf(name).getDescription();
        }
        return "";
    }

    private static boolean containName(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        for (PushRepayMethodType pushRepayMethodType : PushRepayMethodType.values()) {
            if (pushRepayMethodType.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static HashMap<String,String> toMap() {
        HashMap<String,String> map =  new HashMap<>(4);
        for (PushRepayMethodType pushRepayMethodType : PushRepayMethodType.values()) {
            map.put(pushRepayMethodType.name(),pushRepayMethodType.getDescription());
        }
        return map;
    }

}

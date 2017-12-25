package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by pengyueyang on 2016/11/29.
 * 借款期限类型
 */
@AllArgsConstructor
@Getter
public enum LoanTermForRateType {

    YEAS("3200","年"),
    MOTHS("3201","个月"),
    DAYS("3202","天"),
    FIXED_DATE("3203", "天"),
    SEASONS("3204", "季");
    private String code;
    private String description;

    public String toJson() {
        return String.format("{\"code\":\"%s\",\"description\":\"%s\"}", code, description);
    }
    public  static  String getdescription(String code){
        String d="";
        for (LoanTermForRateType e : LoanTermForRateType.values()) {
            if (e.getCode().equals(code)){
                d=e.getDescription();
                break;
            }
        }
        return d;
    }
}

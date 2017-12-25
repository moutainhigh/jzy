package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by pengyueyang on 2016/11/29.
 * 费用类型枚举
 */
@AllArgsConstructor
@Getter
public enum RelationType {

    BR("1","本人"),
    PO("2","配偶"),
    QS("3","亲属"),
    HZR("4","合作人"),
    HZHB("5","合作伙伴"),
    QT("6","其他");
    private String code;
    private String description;

    public  static  String getdescription(String code){
        String d="";
        for (RelationType e : RelationType.values()) {
            if (e.getCode().equals(code)){
                d=e.getDescription();
                break;
            }
        }
        return d;
    }


}

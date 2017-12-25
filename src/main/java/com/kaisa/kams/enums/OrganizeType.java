package com.kaisa.kams.enums;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by pengyueyang on 2016/11/30.
 * 组织机构类型
 */
@Getter
@AllArgsConstructor
public enum OrganizeType {

    AGENCY("1300","机构"),
    ORGANIZE("1301","组织"),
    POSITION("1302","岗位");

    private String code;
    private String description;

    public static OrganizeType getByCode(String code){
        if (StringUtils.isEmpty(code)){
            return null;
        }
        for(OrganizeType organizeType:OrganizeType.values()){
            if(code.equals(organizeType.getCode())){
                return organizeType;
            }
        }
        return null;
    }
}

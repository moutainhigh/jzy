package com.kaisa.kams.enums;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by pengyueyang on 2016/11/30.
 * 业务组织类型
 */
@Getter
@AllArgsConstructor
public enum BusinessOrganizeType {

    AGENCY("1400","部"),
    ORGANIZE("1401","组");

    private String code;
    private String description;

    public static BusinessOrganizeType getByCode(String code){
        if (StringUtils.isEmpty(code)){
            return null;
        }
        for(BusinessOrganizeType organizeType: BusinessOrganizeType.values()){
            if(code.equals(organizeType.getCode())){
                return organizeType;
            }
        }
        return null;
    }
}

package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by zhouchuang on 2017/9/15.
 */
@Getter
@AllArgsConstructor
public enum HolderIdentityType {
    HOUSEHOLDER("001","户主"),
    SPOUSE("002","配偶"),
    CHILDREN("003","子女");

    private String code;
    private String description;
}

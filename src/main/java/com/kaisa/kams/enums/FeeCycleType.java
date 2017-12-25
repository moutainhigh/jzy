package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by yueyang on 2016/11/30.
 * 收费频率类型
 */
@Getter
@AllArgsConstructor
public enum FeeCycleType {

    ONE_TIME("2100","一次性收取"),
    MONTHLY("2101","每期收取");


    private String code;
    private String description;
}

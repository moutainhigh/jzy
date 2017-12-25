package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lw on 2017/11/6.
 * 附属类型
 */
@Getter
@AllArgsConstructor
public enum associationType {

    SUBSIDIARY ("10001","附属单"),
    MASTER ("10002","主单");

    private String code;
    private String description;
}

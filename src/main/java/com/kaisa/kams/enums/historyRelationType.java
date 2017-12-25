package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lw on 2017/11/6.
 * 附属类型
 */
@Getter
@AllArgsConstructor
public enum historyRelationType {

    ORDINARY ("10001","普通关联"),
    SUBSIDIARY ("10002","附属关联");

    private String code;
    private String description;
}

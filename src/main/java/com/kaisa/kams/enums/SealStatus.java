package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lw on 2016/12/15.
 * 用印管理状态枚举
 */
@Getter
@AllArgsConstructor
public enum SealStatus {

    USED("1500","已用印"),
    PREUSED("1501","拟用印"),
    UNUSED("1502","未用印");

    private String code;

    private String description;

    public String toJson() {
        return String.format("{\"code\":\"%s\",\"description\":\"%s\"}", code, description);
    }
}

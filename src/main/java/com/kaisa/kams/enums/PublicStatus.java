package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by pengyueyang on 2016/11/29.
 * 数据公用状态枚举
 */
@Getter
@AllArgsConstructor
public enum PublicStatus {

    DISABLED("1000","不生效"), ABLE("1001","生效");

    private String code;

    private String description;

    public String toJson() {
        return String.format("{\"code\":\"%s\",\"description\":\"%s\"}", code, description);
    }



}

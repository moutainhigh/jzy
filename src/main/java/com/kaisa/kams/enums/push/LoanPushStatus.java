package com.kaisa.kams.enums.push;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lw on 2016/12/28.
 * 推单状态枚举
 */
@Getter
@AllArgsConstructor
public enum LoanPushStatus {

    STAY_PUSH("1600","待推单"),
    PART_PUSHED("1601","部分推单"),
    PUSHED("1602","推单完成");

    private String code;
    private String description;
}

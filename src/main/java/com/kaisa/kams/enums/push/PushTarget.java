package com.kaisa.kams.enums.push;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lw on 2016/12/20.
 * 推送目标状态枚举
 */
@Getter
@AllArgsConstructor
public enum PushTarget {

    KAISAFAX("1600","佳兆业金服");

    private String code;
    private String description;

}

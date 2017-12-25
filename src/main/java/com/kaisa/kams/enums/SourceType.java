package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lw on 2017/11/6.
 * source类型
 */
@Getter
@AllArgsConstructor
public enum SourceType {

    CHANNEL("10001","渠道"),
    BUSINESS("10002","业务");

    private String code;
    private String description;
}

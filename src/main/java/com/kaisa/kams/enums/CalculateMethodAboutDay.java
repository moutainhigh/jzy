package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by pengyueyang on 2016/12/15.
 */

@Getter
@AllArgsConstructor
public enum CalculateMethodAboutDay {

    CALCULATE_HEAD_NOT_TAIL("CMD001","算头不算尾"),
    CALCULATE_TAIL_NOT_HEAD("CMD002","算尾不算头"),
    CALCULATE_HEAD_AND_TAIL("CMD003","算头算尾");

    private String code;
    private String description;

}

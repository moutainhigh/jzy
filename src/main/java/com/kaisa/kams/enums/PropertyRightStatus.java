package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 产权状态
 * Created by lw on 2016/1/6.
 */
@Getter
@AllArgsConstructor
public enum PropertyRightStatus {

    SECURED("5700","已抵押"),
    SOLVED("5701","已解押"),
    WAITSECURED("5702","待抵押");
    //WAITSOLVED("5703","待解押");
    private String code;
    private String description;
}

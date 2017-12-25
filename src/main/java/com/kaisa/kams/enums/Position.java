package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by pengyueyang on 2016/12/6.
 * 职级
 */
@Getter
@AllArgsConstructor
public enum Position {

    S1("S1","见习业务经理"),
    S2("S2","业务经理"),
    S3("S3","初级业务经理"),
    S4("S4","中级业务经理"),
    S5("S5","高级业务经理"),
    S6("S6","资深业务经理"),
    M1("M1","经理"),
    M2("M2","高级经理"),
    M3("M3","资深经理"),
    D1("D1","总监"),
    D2("D2","高级总监"),
    D3("D3","资深总监");

    private String code;
    private String description;
}

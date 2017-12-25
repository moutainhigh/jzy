package com.kaisa.kams.components.utils.pdfUtil;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by zhouchuang on 2017/10/12.
 */
@Getter
@AllArgsConstructor
public enum FontType {

    NORMAL("01","普通文本"),
    TITLE1("02","大标题文本"),
    TITLE2("03","小标题文本"),
    MARK("04","标记文本");
    private String code;
    private String description;

}

package com.kaisa.kams.enums.push;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 项目类型枚举
 * Created by pengyueyang on 2017/11/2.
 */
@Getter
@AllArgsConstructor
public enum ApiResponseType {

    VALIDATE_OK("00", "验证成功"),
    VALIDATE_ERROR_AUTHENTICATION("01", "认证失败"),
    VALIDATE_ERROR_NO_METHOD("02", "没有此方法"),
    VALIDATE_ERROR_ILLEGAL_ARGUMENT("03", "请求参数错误"),
    PROCESS_ERROR("04", "处理异常"),
    PROCESS_OK("06", "处理成功");

    private String code;
    private String description;


}

package com.kaisa.kams.components.params.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author pengyueyang created on 2017/11/23.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiHeadParam {
    /** 请求方法 */
    private String method;
    /** 请求版本 */
    private String version;
    /** 请求appkey */
    private String appKey;
    /** 请求appSercret */
    private String appSecret;
}

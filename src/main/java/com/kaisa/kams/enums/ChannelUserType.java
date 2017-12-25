package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by lw on 2017/11/6.
 * 用户类型
 */
@Getter
@AllArgsConstructor
public enum ChannelUserType {

    CHANNEL_USER("10001","外部渠道人员"),
    COMPANY_USER("10002","内部人员");

    private String code;
    private String description;
}

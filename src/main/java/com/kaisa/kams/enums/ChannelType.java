package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luoyj on 2017/08/08.
 * 渠道类型
 */
@Getter
@AllArgsConstructor
public enum ChannelType {

    ALL ("ALL","全部"),
    ZY ("0","自营"),
    QD ("1","渠道");

    private String code;
    private String description;

}

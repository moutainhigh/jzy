package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 存放状态
 * Created by lw on 2016/1/6.
 */
@Getter
@AllArgsConstructor
public enum StorageStatus {

    IN("6700","入库"),
    OUT("6701","出库");
    private String code;
    private String description;
}

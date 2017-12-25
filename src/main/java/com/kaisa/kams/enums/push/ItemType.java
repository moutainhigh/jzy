package com.kaisa.kams.enums.push;

import org.apache.commons.lang.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 项目类型枚举
 * Created by pengyueyang on 2017/11/2.
 */
@Getter
@AllArgsConstructor
public enum ItemType {

    CAR_LOAN(0, "车贷项目"),
    HOUSE_MORTGAGE_LOAN(1, "红本项目"),
    BANK_HOUSE_LOAN(2, "赎楼项目"),
    PERSONAL_LOAN(3, "个人贷项目"),
    FACTORING(4, "保理项目"),
    BILL(8, "票据项目"),
    BANK_BILL(9, "票据项目");

    private Integer code;
    private String description;

    public static boolean isBill(ItemType itemType) {
        if (BILL.equals(itemType)) {
            return true;
        }
        if (BANK_BILL.equals(itemType)) {
            return true;
        }
        return false;
    }


    public static String getDescriptionByName(String name) {
        if (containName(name)) {
            return ItemType.valueOf(name).getDescription();
        }
        return "";
    }

    private static boolean containName(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        for (ItemType itemType : ItemType.values()) {
            if (itemType.name().equals(name)) {
                return true;
            }
        }
        return false;
    }


}

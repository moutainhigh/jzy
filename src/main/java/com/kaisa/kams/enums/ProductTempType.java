package com.kaisa.kams.enums;

/**
 * Created by sunwanchao on 2017/2/28.
 */
public enum ProductTempType {
    HONGBEN,
    SHULOU,
    PIAOJU,
    YINPIAO,
    CHEDAI,
    GERENDAI,
    RRC,
    BAOLI,
    SHULOUPLAT,
    DIYA,
    JIEYA,
    BROKERAGEFEE;

    public static boolean isBill(ProductTempType type){
        return ProductTempType.PIAOJU.equals(type) || ProductTempType.YINPIAO.equals(type);
    }

    public static boolean hasExtension(ProductTempType productTempType) {
        if (HONGBEN.equals(productTempType)) {
            return true;
        }
        if (SHULOU.equals(productTempType)) {
            return true;
        }
        if (GERENDAI.equals(productTempType)) {
            return true;
        }
        return false;
    }
}

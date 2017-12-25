package com.kaisa.kams.components.utils;

import com.kaisa.kams.enums.LoanTermType;

import org.apache.commons.lang.StringUtils;

/**
 * Created by liuwen01 on 2017/8/17.
 */
public class TransformUtil {

    public static String getTermType(LoanTermType termType, String term){
        if (null == termType || StringUtils.isEmpty(term)) {
            return null;
        }
        if(LoanTermType.YEAS.equals(termType)){
            return term+"年";
        }else if(LoanTermType.MOTHS.equals(termType)){
            return term+"个月";
        }else if(LoanTermType.DAYS.equals(termType)){
            return  term+"天";
        }else if(LoanTermType.FIXED_DATE.equals(termType)){
            return  "至"+term;
        }else if(LoanTermType.SEASONS.equals(termType)){
            return  term+"季";
        }
        return null;
    }
}

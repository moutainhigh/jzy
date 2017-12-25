package com.kaisa.kams.components.utils;

import java.math.BigDecimal;

/**
 * Created by pengyueyang on 2017/1/7.
 */
public class DecimalFormatUtils {

    public static BigDecimal removeZeroFormat(BigDecimal source) {
        if (null != source) {
            String tmp = source.toString();
            tmp = removeZeroFormat(tmp);
            if (null != tmp) {
                try {
                    return new BigDecimal(tmp);
                }catch (NumberFormatException e) {
                    return BigDecimal.ZERO;
                }
            }
        }
        return source;
    }

    public static String removeZeroFormat(String source) {
        if (null != source) {
            int index = source.indexOf(".");
            if (index==-1 || index==source.length()-1) {
                return source;
            }
            int end = source.length()-1;
            for (int i=source.length()-1;i>=0;i--) {
                if (source.charAt(i)=='0' && i>index) {
                    continue;
                }else {
                    end = i+1;
                    break;
                }
            }
            source = source.substring(0,end);
            if (source.indexOf(".")==source.length()-1) {
                end = end-1;
            }
            return source.substring(0,end);
        }
        return source;
    }


    public static String removeZeroFormat(Object source) {
        if (null != source) {
            try {
                return removeZeroFormat((String) source);
            } catch (ClassCastException e) {
                return null;
            }
        }
        return null;
    }

    public static boolean  isEmpty(BigDecimal bigDecimal){
        return bigDecimal==null||bigDecimal.doubleValue()==0D;
    }
    public static boolean  isNotEmpty(BigDecimal bigDecimal){
        return !isEmpty(bigDecimal);
    }

    public static BigDecimal getNotNull(BigDecimal bigDecimal){
        return isEmpty(bigDecimal)?BigDecimal.ZERO:bigDecimal;
    }
    public static int compare(BigDecimal a,BigDecimal b){
        return  getNotNull(a).compareTo(getNotNull(b));
    }

}

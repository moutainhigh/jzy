package com.kaisa.kams.components.utils;

import java.math.BigDecimal;

/**
 * Created by sunwanchao on 2017/1/12.
 */
public class DecimalUtils {
    public static BigDecimal sum(BigDecimal firstNumber, BigDecimal secondNumber){
        return (firstNumber==null?BigDecimal.ZERO:firstNumber).add(secondNumber==null?BigDecimal.ZERO:secondNumber);
    }
    public static BigDecimal sumArr(BigDecimal... arr){
        BigDecimal result = BigDecimal.ZERO;
        for(BigDecimal decimal:arr){
            result = result.add(decimal==null?BigDecimal.ZERO:decimal);
        }
        return result;
    }
    public static BigDecimal sub(BigDecimal minuend, BigDecimal subtrahend){
        return (minuend==null?BigDecimal.ZERO:minuend).subtract(subtrahend==null?BigDecimal.ZERO:subtrahend);
    }
}

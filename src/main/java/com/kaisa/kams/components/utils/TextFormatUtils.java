package com.kaisa.kams.components.utils;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * Created by wangqx on 2017/2/13.
 */
public class TextFormatUtils {
    /**
     * 格式化银行账号
     * @param account
     * @return
     */
    public static String formatAccount(String account) {
        if (StringUtils.isNotEmpty(account)) {
            return account.replaceAll("([\\d]{4})(?=\\d)", "$1 ");
        }
        return "";
    }
    public static String replaceNull(String text,String replace){
        if(StringUtils.isEmpty(text))return replace;
        else return text;
    }

    public static String formatBigDecimal(BigDecimal bigDecimal) {
        String result;
        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits( 2 );
        if (null != bigDecimal) {
            result = format.format(bigDecimal);
            return result;
        }
        return "";
    }

    public static void main(String[] args)throws Exception
    {
       String a =  formatBigDecimal(new BigDecimal(30000.00));
        System.out.print(a);
    }
}

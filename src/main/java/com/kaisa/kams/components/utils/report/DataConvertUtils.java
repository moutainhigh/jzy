package com.kaisa.kams.components.utils.report;

/**
 * Created by pengyueyang on 2017/10/18.
 */
public class DataConvertUtils {

    public static String formatTerm(int totalDays) {
        if (totalDays <= 7) {
            return "1D-7D";
        }
        if (totalDays <= 15) {
            return "8D-15D";
        }
        if (totalDays <= 20) {
            return "16D-20D";
        }
        if (totalDays <= 44) {
            return "21D-1M";
        }
        int month = ((totalDays+15)/30);
        month=Math.min(12,month);
        return  month + "M";
    }
}

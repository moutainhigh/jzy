package com.kaisa.kams.components.utils.report;

import java.math.BigDecimal;

/**
 * Created by wangqx on 2017/6/29.
 */
public class ReportUtils {
    private static final BigDecimal YEAR_DAYS_365 = new BigDecimal(365);
    private static final BigDecimal YEAR_MONTHS = new BigDecimal(12);

    private static final String YEAR_CN = "年";
    private static final String MONTH_CN = "月";
    private static final String DAY_CN = "天";
    private static final String FIXED_DATE_CN = "至";


    public static BigDecimal getYearRateByDayInterestRate(BigDecimal interestRate) {
        BigDecimal yearRate = BigDecimal.ZERO;
        if (null != interestRate) {
            return interestRate.multiply(YEAR_DAYS_365);
        }
        return yearRate;
    }

    public static BigDecimal getYearRateByMonthInterestRate(BigDecimal interestRate) {
        BigDecimal yearRate = BigDecimal.ZERO;
        if (null != interestRate) {
            return interestRate.multiply(YEAR_MONTHS);
        }
        return yearRate;
    }

    public static String getYearsLoanTerm(String term) {
        return String.format("%s%s",term,YEAR_CN);
    }

    public static String getMonthsLoanTerm(String term) {
        return String.format("%s个%s",term,MONTH_CN);
    }

    public static String getDaysLoanTerm(String term) {
        return String.format("%s%s",term,DAY_CN);
    }

    public static String getFixedDateLoanTerm(String term) {
        return String.format("%s%s",FIXED_DATE_CN,term);
    }

    public static BigDecimal bigDecimalCompare(BigDecimal val){
        BigDecimal result = null;
        if(null != val){
            result = (val).compareTo(BigDecimal.ZERO) >=0 ? val : new BigDecimal(0.00);
        }
        return result;
    }

    public static BigDecimal bigDecimalCompareClear(BigDecimal val){
        BigDecimal result = null;
        if(null != val){
            result = (val).compareTo(BigDecimal.ZERO) !=0 ? new BigDecimal(0.00) : val;
        }
        return result;
    }

}

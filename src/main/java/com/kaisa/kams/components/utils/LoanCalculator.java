package com.kaisa.kams.components.utils;

import com.kaisa.kams.components.view.loan.*;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.enums.LoanRepayMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import static com.kaisa.kams.components.view.loan.NumberConstant.*;
import static com.kaisa.kams.enums.LoanRepayMethod.*;

/**
 * Created by sunwanchao on 2016/12/12.
 */
public final class LoanCalculator {

    private static final BigDecimal MONTHS_PER_YEAR = new BigDecimal(TimeConstant.MONTHS_PER_YEAR);

    private static final BigDecimal DAYS_PER_YEAR = new BigDecimal(TimeConstant.DAYS_PER_YEAR);

    private static final BigDecimal RATE_FACTOR = new BigDecimal(NumberConstant.RATE_FACTOR);

    private static final RoundingMode RUNDING_MODE = RoundingMode.HALF_EVEN;


    /**
     * @param amount
     * @param loanTermType  产品借款期限 (1:天,2:月,3:固定时间)
     * @param dm            天或月数字
     * @param repayMethod   还款方式(INTEREST:先息后本,BULLET_REPAYMENT:一次性还本付息,EQUAL_INSTALLMENT:等额本息)
     * @param loanLimitType 计息方式(1:固定费用,2:固定费率)
     * @param feeFr         固定费用或固定费率数字
     * @param repayDateType 还款时间 (1:期初收息,2:期末收息)
     * @return
     */
    public static LoanRepayPlan calcuate(BigDecimal amount,
                                         LoanTermType loanTermType,
                                         String dm,
                                         LoanRepayMethod repayMethod,
                                         LoanLimitType loanLimitType,
                                         BigDecimal feeFr,
                                         LoanRepayDateType repayDateType,
                                         Date loanTime,
                                         BigDecimal minInterestAmount,
                                         CalculateMethodAboutDay aboutDay) {
        LoanRepayPlan repayPlan = null;
        switch (loanTermType) {
            case DAYS:
                if (INTEREST_DAYS.equals(repayMethod) || BULLET_REPAYMENT.equals(repayMethod)) {
                    repayPlan = calcD(amount, Integer.parseInt(dm), repayMethod, loanLimitType, feeFr, repayDateType, loanTime, minInterestAmount, aboutDay);
                }
                break;
            case MOTHS:
                if (INTEREST_MONTHS.equals(repayMethod) || EQUAL_INSTALLMENT.equals(repayMethod) || BULLET_REPAYMENT.equals(repayMethod)) {
                    repayPlan = calcM(amount, new Duration(0, Integer.parseInt(dm), 0), repayMethod, loanLimitType, feeFr, repayDateType, loanTime,minInterestAmount);
                }
                break;
            case YEAS:
                if (INTEREST_MONTHS.equals(repayMethod) || EQUAL_INSTALLMENT.equals(repayMethod)) {
                    repayPlan = calcM(amount, new Duration(Integer.parseInt(dm), 0, 0), repayMethod, loanLimitType, feeFr, repayDateType, loanTime,minInterestAmount);
                }
                break;
            case FIXED_DATE:
                if (INTEREST_DAYS.equals(repayMethod) || BULLET_REPAYMENT.equals(repayMethod)) {
                    repayPlan = calcF(amount, dm, repayMethod, loanLimitType, feeFr, repayDateType, loanTime, minInterestAmount, aboutDay);
                }
                break;
            case SEASONS:
                if (INTEREST_SEASONS.equals(repayMethod)) {
                    repayPlan = calcS(amount, new Duration(0, Integer.parseInt(dm)*3, 0), loanLimitType, feeFr, repayDateType, loanTime);
                }
                break;
            default:
                break;
        }
        return repayPlan;
    }


    /**
     * 借款期限为天的还款计划
     *
     * @param amount
     * @param d
     * @param repayMethod
     * @param loanLimitType
     * @param feeFr
     * @param repayDateType
     */
    private static LoanRepayPlan calcD(BigDecimal amount,
                                       int d,
                                       LoanRepayMethod repayMethod,
                                       LoanLimitType loanLimitType,
                                       BigDecimal feeFr,
                                       LoanRepayDateType repayDateType,
                                       Date loanTime,
                                       BigDecimal minInterestAmount,
                                       CalculateMethodAboutDay aboutDay) {
        int day = d - 1;
        if (CalculateMethodAboutDay.CALCULATE_HEAD_NOT_TAIL.equals(aboutDay)) {
            //算头不算尾
            day = d;
        } else if (CalculateMethodAboutDay.CALCULATE_TAIL_NOT_HEAD.equals(aboutDay)) {
            //算尾不算头
            day = d;
        }
        Duration duration = new Duration(0, 0, day);
        LoanRepayPlan result;
        BigDecimal ratePerDay = feeFr.divide(RATE_FACTOR, MATH_CONTEXT);//日利率
        BigDecimal totalPrincipal = amount.setScale(SCALE, RUNDING_MODE), interest, principal, outstanding = totalPrincipal;

        minInterestAmount = minInterestAmount == null ? BigDecimal.ZERO : minInterestAmount;
        switch (repayMethod) {
            case INTEREST_DAYS:
                interest = totalPrincipal.multiply(ratePerDay).multiply(new BigDecimal(d));
                if (LoanLimitType.FIX_AMOUNT.equals(loanLimitType)) {
                    interest = feeFr.multiply(new BigDecimal(d));
                }
                interest = interest.compareTo(minInterestAmount) > 0 ? interest : minInterestAmount;
                result = new LoanRepayPlan(totalPrincipal, interest, duration, INTEREST_DAYS);
                if (LoanRepayDateType.REPAY_PRE.equals(repayDateType)) {
                    result.getRepayments().add(new Repayment(DEFAULT_ZERO,
                            interest,
                            totalPrincipal,
                            loanTime,
                            1));
                    result.getRepayments().add(new Repayment(totalPrincipal,
                            DEFAULT_ZERO,
                            DEFAULT_ZERO,
                            TimeUtils.offset(loanTime, duration),
                            2));
                } else {
                    result.getRepayments().add(new Repayment(totalPrincipal,
                            interest,
                            DEFAULT_ZERO,
                            TimeUtils.offset(loanTime, duration),
                            1));
                }
                return result;
            case BULLET_REPAYMENT:
                interest = totalPrincipal.multiply(ratePerDay).multiply(new BigDecimal(d));
                if (LoanLimitType.FIX_AMOUNT.equals(loanLimitType)) {
                    interest = feeFr.multiply(new BigDecimal(d));
                }
                interest = interest.compareTo(minInterestAmount) > 0 ? interest : minInterestAmount;
                interest = interest.setScale(SCALE, RUNDING_MODE);
                result = new LoanRepayPlan(totalPrincipal, interest, duration, BULLET_REPAYMENT);
                result.getRepayments().add(new Repayment(totalPrincipal,
                        interest,
                        DEFAULT_ZERO,
                        TimeUtils.offset(loanTime, duration),
                        1));
                return result;
            default:
                return null;
        }
    }

    /**
     * 借款期限为月的还款计划
     *
     * @param amount
     * @param duration
     * @param repayMethod
     * @param loanLimitType
     * @param feeFr
     * @param repayDateType
     */
    private static LoanRepayPlan calcM(BigDecimal amount,
                                       Duration duration,
                                       LoanRepayMethod repayMethod,
                                       LoanLimitType loanLimitType,
                                       BigDecimal feeFr,
                                       LoanRepayDateType repayDateType,
                                       Date loanTime,
                                       BigDecimal minInterestAmount) {
        LoanRepayPlan result;
        BigDecimal ratePerMonth = feeFr.divide(RATE_FACTOR, MATH_CONTEXT);//月利率
        BigDecimal totalPrincipal = amount.setScale(SCALE, RUNDING_MODE), totalInterest = DEFAULT_ZERO, interest, principal, outstanding = totalPrincipal;
        int periods;
        minInterestAmount = minInterestAmount == null ? BigDecimal.ZERO : minInterestAmount;
        switch (repayMethod) {
            case INTEREST_MONTHS:
                interest = totalPrincipal.multiply(ratePerMonth).setScale(SCALE, RUNDING_MODE);
                if (LoanLimitType.FIX_AMOUNT.equals(loanLimitType)) {
                    interest = feeFr;
                }
                totalInterest = interest.multiply(new BigDecimal(duration.getTotalMonths()));
                result = new LoanRepayPlan(totalPrincipal, totalInterest, duration, INTEREST_MONTHS);
                if (LoanRepayDateType.REPAY_PRE.equals(repayDateType)) {//期初
                    result.getRepayments().add(new Repayment(DEFAULT_ZERO, interest, totalPrincipal, loanTime, 1));
                }
                for (int i = 1; i <= duration.getTotalMonths(); i++) {
                    if (i < duration.getTotalMonths()) {
                        result.getRepayments().add(new Repayment(DEFAULT_ZERO,
                                interest,
                                totalPrincipal,
                                TimeUtils.offset(loanTime, new Duration(0, i, 0)),
                                LoanRepayDateType.REPAY_PRE.equals(repayDateType) ? i + 1 : i));
                    } else {
                        result.getRepayments().add(new Repayment(totalPrincipal,
                                LoanRepayDateType.REPAY_PRE.equals(repayDateType) ? DEFAULT_ZERO : interest,
                                DEFAULT_ZERO,
                                TimeUtils.offset(loanTime, new Duration(0, i, 0)),
                                LoanRepayDateType.REPAY_PRE.equals(repayDateType) ? i + 1 : i));
                    }
                }
                return result;
            case EQUAL_INSTALLMENT:
                //等额本息不存在固定费用
                periods = duration.getTotalMonths();
                BigDecimal baseInterest = totalPrincipal.multiply(ratePerMonth);
                BigDecimal[] powers = new BigDecimal[periods + 1];
                for (int i = 0; i <= periods; i++) {
                    powers[i] = ratePerMonth.add(DEFAULT_ONE).pow(i);
                }
                BigDecimal installment = baseInterest.multiply(powers[periods])
                        .divide(powers[periods].subtract(DEFAULT_ONE), MATH_CONTEXT)
                        .setScale(SCALE, RUNDING_MODE);
                result = new LoanRepayPlan(totalPrincipal, totalInterest, duration, EQUAL_INSTALLMENT);
                for (int i = 0; i < periods; i++) {
                    interest = baseInterest.subtract(installment, MATH_CONTEXT)
                            .multiply(powers[i])
                            .add(installment, MATH_CONTEXT)
                            .setScale(SCALE, RUNDING_MODE);
                    principal = installment.subtract(interest);
                    outstanding = outstanding.subtract(principal);
                    if (i < periods - 1) {
                        result.getRepayments().add(new Repayment(principal,
                                interest,
                                outstanding,
                                TimeUtils.offset(loanTime, new Duration(0, i + 1, 0)),
                                i + 1));
                    } else {
                        result.getRepayments().add(new Repayment(principal.add(outstanding),
                                interest,
                                DEFAULT_ZERO,
                                TimeUtils.offset(loanTime, new Duration(0, i + 1, 0)),
                                i + 1));
                    }
                    totalInterest = totalInterest.add(interest);
                }
                result.setInterest(totalInterest);
                return result;
            case BULLET_REPAYMENT:
                interest = totalPrincipal.multiply(ratePerMonth).multiply(new BigDecimal(duration.getMonths()));
                if (LoanLimitType.FIX_AMOUNT.equals(loanLimitType)) {
                    interest = feeFr.multiply(new BigDecimal(duration.getMonths()));
                }
                interest = interest.setScale(SCALE, RUNDING_MODE);
                interest = interest.compareTo(minInterestAmount) > 0 ? interest : minInterestAmount;
                result = new LoanRepayPlan(totalPrincipal, interest, duration, BULLET_REPAYMENT);
                result.getRepayments().add(new Repayment(totalPrincipal,
                        interest,
                        DEFAULT_ZERO,
                        TimeUtils.offset(loanTime, duration),
                        1));
                return result;
            default:
                return null;
        }
    }

    /**
     * 借款期限为固定时间的还款计划
     *
     * @param amount
     * @param repayMethod
     * @param loanLimitType
     * @param feeFr
     * @param repayDateType
     */
    private static LoanRepayPlan calcF(BigDecimal amount,
                                       String d,
                                       LoanRepayMethod repayMethod,
                                       LoanLimitType loanLimitType,
                                       BigDecimal feeFr,
                                       LoanRepayDateType repayDateType,
                                       Date loanTime,
                                       BigDecimal minInterestAmount,
                                       CalculateMethodAboutDay aboutDay) {
        Date repayDate = TimeUtils.formatDate("yyyy-MM-dd", d);
        int day = TimeUtils.daysBetween(loanTime, repayDate) + 1;
        if (CalculateMethodAboutDay.CALCULATE_HEAD_NOT_TAIL.equals(aboutDay)) {
            //算头不算尾
            day = day - 1;
        } else if (CalculateMethodAboutDay.CALCULATE_TAIL_NOT_HEAD.equals(aboutDay)) {
            //算尾不算头
            day = day - 1;
        }
        Duration duration = new Duration(0, 0, day);
        LoanRepayPlan result;
        BigDecimal ratePerDay = feeFr.divide(RATE_FACTOR, MATH_CONTEXT);//日利率
        BigDecimal totalPrincipal = amount.setScale(SCALE, RUNDING_MODE), interest, principal, outstanding = totalPrincipal;
        
        minInterestAmount = minInterestAmount == null ? BigDecimal.ZERO : minInterestAmount;
        switch (repayMethod) {
            case INTEREST_DAYS:
                interest = totalPrincipal.multiply(ratePerDay).multiply(new BigDecimal(duration.getDays()));
                if (LoanLimitType.FIX_AMOUNT.equals(loanLimitType)) {
                    interest = feeFr.multiply(new BigDecimal(duration.getDays()));
                }
                interest = interest.compareTo(minInterestAmount)>0?interest:minInterestAmount;
                result = new LoanRepayPlan(totalPrincipal, interest, duration, INTEREST_DAYS);
                if (LoanRepayDateType.REPAY_PRE.equals(repayDateType)) {
                    result.getRepayments().add(new Repayment(DEFAULT_ZERO,
                            interest,
                            totalPrincipal,
                            loanTime,
                            1));
                    result.getRepayments().add(new Repayment(totalPrincipal,
                            DEFAULT_ZERO,
                            DEFAULT_ZERO,
                            repayDate,
                            2));
                } else {
                    result.getRepayments().add(new Repayment(totalPrincipal,
                            interest,
                            DEFAULT_ZERO,
                            repayDate,
                            1));
                }
                return result;
            case BULLET_REPAYMENT:
                interest = totalPrincipal.multiply(ratePerDay).multiply(new BigDecimal(duration.getDays()));
                if (LoanLimitType.FIX_AMOUNT.equals(loanLimitType)) {
                    interest = feeFr.multiply(new BigDecimal(duration.getDays()));
                }
                interest = interest.compareTo(minInterestAmount)>0?interest:minInterestAmount;
                interest = interest.setScale(SCALE, RUNDING_MODE);
                result = new LoanRepayPlan(totalPrincipal, interest, duration, BULLET_REPAYMENT);
                result.getRepayments().add(new Repayment(totalPrincipal,
                        interest,
                        DEFAULT_ZERO,
                        repayDate,
                        1));
                return result;
            default:
                return null;
        }
    }



    /**
     * 借款期限为季度的还款计划
     *
     * @param amount
     * @param duration
     * @param loanLimitType
     * @param feeFr
     * @param repayDateType
     */
    private static LoanRepayPlan calcS(BigDecimal amount,
                                       Duration duration,
                                       LoanLimitType loanLimitType,
                                       BigDecimal feeFr,
                                       LoanRepayDateType repayDateType,
                                       Date loanTime) {
        LoanRepayPlan result;
        BigDecimal ratePerSeasons = feeFr.divide(RATE_FACTOR, MATH_CONTEXT);//季度利率
        BigDecimal totalPrincipal = amount.setScale(SCALE, RUNDING_MODE), totalInterest = DEFAULT_ZERO, interest;
        interest = totalPrincipal.multiply(ratePerSeasons).setScale(SCALE, RUNDING_MODE);
        if (LoanLimitType.FIX_AMOUNT.equals(loanLimitType)) {
            interest = feeFr;
        }
        int seaSonsNum=duration.getTotalMonths()/3;
        totalInterest = interest.multiply(new BigDecimal(seaSonsNum));
        result = new LoanRepayPlan(totalPrincipal, totalInterest, duration, INTEREST_MONTHS);
        if (LoanRepayDateType.REPAY_PRE.equals(repayDateType)) {//期初
            result.getRepayments().add(new Repayment(DEFAULT_ZERO, interest, totalPrincipal, loanTime, 1));
        }
        for (int i = 1; i <= seaSonsNum; i++) {
            if (i < seaSonsNum) {
                result.getRepayments().add(new Repayment(DEFAULT_ZERO,
                        interest,
                        totalPrincipal,
                        TimeUtils.offset(loanTime, new Duration(0, i*3, 0)),
                        LoanRepayDateType.REPAY_PRE.equals(repayDateType) ? i + 1 : i));
            } else {
                result.getRepayments().add(new Repayment(totalPrincipal,
                        LoanRepayDateType.REPAY_PRE.equals(repayDateType) ? DEFAULT_ZERO : interest,
                        DEFAULT_ZERO,
                        TimeUtils.offset(loanTime, new Duration(0, i*3, 0)),
                        LoanRepayDateType.REPAY_PRE.equals(repayDateType) ? i + 1 : i));
            }
        }
        return result;
    }

}

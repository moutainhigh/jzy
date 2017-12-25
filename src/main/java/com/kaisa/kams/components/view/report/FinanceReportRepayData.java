package com.kaisa.kams.components.view.report;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Created by wangqx on 2017/8/7.
 */
@Data
@NoArgsConstructor
public class FinanceReportRepayData {
    //应收本金
    private BigDecimal receivablePrincipal;
    //实还本金
    private BigDecimal repayAmount;
    //未还本金
    private BigDecimal outstandingAmount;
    //应收利息
    private BigDecimal receivableInterest;
    //实还利息
    private BigDecimal repayInterest;
    //未还利息
    private BigDecimal outstandingInterest;
    //应收费用
    private BigDecimal receivableFee;
    //实还费用
    private BigDecimal repayFee;
    //未还费用
    private BigDecimal outstandingFee;
    //应收合计
    private BigDecimal receivableTotal;
    //实还合计
    private BigDecimal repayTotal;
    //未还合计
    private BigDecimal outstandingTotal;

    public void setOherVal(){
        this.setOutstandingAmount(receivablePrincipal.subtract(repayAmount));
        this.setOutstandingInterest(bigDecimalCompare(receivableInterest.subtract(repayInterest)));
        this.setOutstandingFee(receivableFee.subtract(repayFee));
        this.setReceivableTotal(receivablePrincipal.add(receivableInterest.add(receivableFee)));
        this.setRepayTotal(repayAmount.add(repayInterest.add(repayFee)));
        this.setOutstandingTotal(bigDecimalCompare(outstandingAmount.add(outstandingInterest.add(outstandingFee))));
    }

    private BigDecimal bigDecimalCompare(BigDecimal val){
        BigDecimal result = null;
        if(null != val){
            result = (val).compareTo(BigDecimal.ZERO) >=0 ? val : new BigDecimal(0.00);
        }
        return result;
    }

}

package com.kaisa.kams.components.view.report;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wangqx on 2017/8/7.
 */
@Data
@NoArgsConstructor
public class BusinessReportFeeData {

    /* 一次性服务费 */
    private BigDecimal prePaymentFeeAmount;
    /* 实收一次性服务费 */
    private BigDecimal actualPrePaymentFeeAmount;
    /* 实收罚息 */
    private BigDecimal actualOverdueFeeAmount;
    /* 应收总费服务费（包含罚息） */
    private BigDecimal totalServiceFeeAmount;
    /* 实收总费服务费（包含罚息） */
    private BigDecimal actualServiceTotalFeeAmount;
    /* 借款服务费 */
    private BigDecimal serviceFeeAmount;
    /* 实收借款服务费（元） */
    private BigDecimal actualServiceFeeAmount;

    public static BusinessReportFeeData create() {
        BusinessReportFeeData data = new BusinessReportFeeData();
        data.setPrePaymentFeeAmount(BigDecimal.ZERO);
        data.setActualPrePaymentFeeAmount(BigDecimal.ZERO);
        data.setActualOverdueFeeAmount(BigDecimal.ZERO);
        data.setTotalServiceFeeAmount(BigDecimal.ZERO);
        data.setActualServiceTotalFeeAmount(BigDecimal.ZERO);
        data.setServiceFeeAmount(BigDecimal.ZERO);
        data.setActualServiceFeeAmount(BigDecimal.ZERO);
        return data;
    }

}

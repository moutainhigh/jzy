package com.kaisa.kams.components.view.report;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wangqx on 2017/8/7.
 */
@Data
@NoArgsConstructor
public class BusinessReportRepayData {
    /* 实还本金 */
    private BigDecimal actualAmount;
    /* 实还利息 */
    private BigDecimal actualInterest;

    public static BusinessReportRepayData create() {
        BusinessReportRepayData data = new BusinessReportRepayData();
        data.setActualAmount(BigDecimal.ZERO);
        data.setActualInterest(BigDecimal.ZERO);
        return data;
    }
}

package com.kaisa.kams.components.view.report;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wangqx on 2017/8/10.
 */
@Data
@NoArgsConstructor
public class BusinessReportLastRepayData {

    //近期收款日期
    private String lastRepayDate;
    //近期收款费用
    private BigDecimal lastRepayAmount;

    public static BusinessReportLastRepayData create() {
        BusinessReportLastRepayData data = new BusinessReportLastRepayData();
        data.setLastRepayDate("");
        data.setLastRepayAmount(BigDecimal.ZERO);
        return data;
    }
}

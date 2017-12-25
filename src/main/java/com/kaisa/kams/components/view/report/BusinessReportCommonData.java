package com.kaisa.kams.components.view.report;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wangqx on 2017/8/7.
 */
@Data
@NoArgsConstructor
public class BusinessReportCommonData {
    /* 应还和实收费用信息 */
    private BusinessReportFeeData feeData;
    /* 实还本金和利息信息 */
    private BusinessReportRepayData repayData;
    /* 实收总费用 = 实收总服务费 + 实收利息 */
    private BigDecimal actualTotalFeeAmount;
    /* 实还总额 = 实还本金 + 实收总费用 */
    private BigDecimal actualTotalAmount;

    public static BusinessReportCommonData create() {
        BusinessReportCommonData data = new BusinessReportCommonData();
        data.setFeeData(BusinessReportFeeData.create());
        data.setRepayData(BusinessReportRepayData.create());
        data.setActualTotalFeeAmount(BigDecimal.ZERO);
        data.setActualTotalAmount(BigDecimal.ZERO);
        return data;
    }


}

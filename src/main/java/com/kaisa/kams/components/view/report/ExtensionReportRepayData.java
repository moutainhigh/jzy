package com.kaisa.kams.components.view.report;

import com.kaisa.kams.components.utils.DecimalUtils;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author pengyueyang created on 2017/12/14.
 */
@Data
@NoArgsConstructor
public class ExtensionReportRepayData {

    /* 展期到期日 */
    private Date extensionDueDate;

    /* 展期应还本金（元）*/
    private BigDecimal extensionAmount;

    /* 展期实还本金（元）*/
    private BigDecimal extensionRepayAmount;

    /* 展期未还本金（元）*/
    private BigDecimal extensionRemainAmount;

    /* 展期应还利息（元）*/
    private BigDecimal extensionInterest;

    /* 展期实还利息（元）*/
    private BigDecimal extensionRepayInterest;

    /* 展期未还利息（元）*/
    private BigDecimal extensionRemainInterest;

    public BigDecimal getExtensionRemainAmount() {
        return DecimalUtils.sub(extensionAmount, extensionRepayAmount);
    }

    public BigDecimal getExtensionRemainInterest() {
        return DecimalUtils.sub(extensionInterest, extensionRepayInterest);
    }

}

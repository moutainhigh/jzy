package com.kaisa.kams.components.view.push;

import com.kaisa.kams.enums.LoanTermType;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推单订单
 * @author pengyueyang created on 2017/11/9.
 */
@Data
@NoArgsConstructor
public class LoanPushOrderView {
    /** id */
    private String id;

    /** 推送目标 */
    private String pushTarget;

    /** 资产单号 */
    private String code;

    /** 对应项目 */
    private String itemType;

    /** 标的单号 */
    private String platformLoanCode;

    /** 标的金额 */
    private BigDecimal amount;

    /** 期限 */
    private String term;

    /** 期限类型 */
    private LoanTermType termType;

    /** 期限描述 */
    private String termStr;

    /** 还款方式 */
    private String repayMethod;

    /** 渠道资金利率 */
    private BigDecimal channelRate;

    /** 推单时间 */
    private Date pushDateTime;

    /** 标的放款日期 */
    private Date platformLoanTime;

    /** 标的利率 */
    private BigDecimal platformLoanRate;

    /** 平台标的总期数 */
    private Integer platformLoanTotalPeriod;

    /** 平台标的当前期数 */
    private Integer platformLoanCurrentPeriod;

    /** 平台标的下期还款时间 */
    private Date platformLoanNextDueDate;

    /** 标的应结清日期 */
    private Date platformLoanDueDate;

    /** 标的实际结清日期 */
    private Date platformLoanClearedDate;

    /** 状态 */
    private String status;

    /** 审批任务id */
    private String taskId;
}

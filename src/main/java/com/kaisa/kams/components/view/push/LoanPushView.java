package com.kaisa.kams.components.view.push;

import com.kaisa.kams.enums.LoanTermType;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author pengyueyang created on 2017/11/7.
 * 推单view
 */
@Data
@NoArgsConstructor
public class LoanPushView {
    /** id */
    private String id;

    /** 业务单id */
    private String loanId;

    /** 业务单号 */
    private String loanCode;

    /** 产品大类名称 */
    private String productTypeName;

    /** 产品子名称 */
    private String productName;

    /** 放款主体 */
    private String loanSubjectName;

    /** 业务来源 */
    private String source;

    /** 渠道 */
    private String channelName;

    /** 来源类型（自营0、渠道1）*/
    private String sourceType;

    /** 业务条线 */
    private String businessLine;

    /** 组织代码 */
    private String organizeCode;

    /** 业务员姓名 */
    private String saleName;

    /** 借款人 */
    private String masterBorrowerName;

    /** 借款期限 */
    private String term;

    /** 借款期限类型 */
    private LoanTermType termType;

    /** 借款期限前端显示 */
    private String termStr;

    /** 高管审批金额 */
    private BigDecimal amount;

    /** 高管审批时间 */
    private Date leaderApprovedTime;

    /** 放款时间 */
    private Date loanTime;

    /** 放款金额 */
    private BigDecimal actualAmount;

    /** 应结清日期 */
    private String dueDate;

    /** 业务单状态 */
    private String loanStatus;

    /** 实际结清日期 */
    private Date clearDate;

    /** 标的最大应结清日期 */
    private Date loanMaxDueDate;

    /** 标的最小应结清日期 */
    private Date loanMinDueDate;

    /** 业务单提交时间 */
    private Date submitTime;

    /** 业务单创建人 */
    private String loanCreateBy;

    /** 是否展期 */
    private Boolean extensionLabel;

    /** 推单状态 */
    private String status;

}

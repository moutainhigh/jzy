package com.kaisa.kams.components.params.push;

import com.kaisa.kams.components.params.base.DataTableBaseParam;
import com.kaisa.kams.components.utils.TimeUtils;
import com.kaisa.kams.enums.push.LoanPushStatus;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author pengyueyang created on 2017/11/7.
 */
@Data
@NoArgsConstructor
public class DataTableLoanPushParam extends DataTableBaseParam{

    /** 业务单号 */
    private String loanCode;

    /** 业务人员 */
    private String saleName;

    /** 借款人 */
    private String masterBorrowerName;

    /** 渠道名称 */
    private String channelName;

    /** 产品大类id */
    private String productTypeId;

    /** 产品子类id */
    private String productId;

    /** 放款主体id */
    private String loanSubjectId;

    /** 标的最大应结清日 */
    private String loanMaxDueDate;

    /** 标的最大应结清查询开始日期 */
    private String loanMaxBeginDueDate;

    /** 标的最大应结清查询结束日期 */
    private String loanMaxEndDueDate;

    /** 推单状态 */
    private LoanPushStatus loanPushStatus;


    public Date getLoanMaxBeginDueDate() {
        return TimeUtils.getQueryStartDateTime(loanMaxDueDate);
    }

    public Date getLoanMaxEndDueDate() {
        return TimeUtils.getQueryEndDateTime(loanMaxDueDate);
    }
}

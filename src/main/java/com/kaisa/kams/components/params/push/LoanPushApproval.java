package com.kaisa.kams.components.params.push;

import com.kaisa.kams.enums.ApprovalCode;
import com.kaisa.kams.enums.ApprovalType;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推单审批参数
 * @author pengyueyang created on 2017/11/17.
 */
@Data
@NoArgsConstructor
public class LoanPushApproval {
    /** 业务推单id*/
    private String loanPushId;
    /** 推单订单Id */
    private String loanPushOrderId;
    /** 审批订单Id */
    private String orderId;
    /** 任务id */
    private String taskId;
    /** 审批结果类型 */
    private ApprovalCode approvalCode;
    /** 审批类型 */
    private ApprovalType approvalType;
    /** 审批意见 */
    private String content;
    /** 是否需要重新走流程 */
    private boolean needRepeatFlow;
}

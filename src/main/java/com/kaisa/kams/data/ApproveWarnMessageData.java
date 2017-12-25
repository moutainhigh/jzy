package com.kaisa.kams.data;

import com.kaisa.kams.models.Loan;
import com.kaisa.kams.models.Mortgage;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by peng on 2017/8/14.
 * 业务审批短信
 */
@Data
@NoArgsConstructor
public class ApproveWarnMessageData {
    /* 审批任务id */
    private String taskId;
    /* 业务单id */
    private String loanId;
    /* 业务单code */
    private String loanCode;
    /* 大类id */
    private String productTypeId;
    /* 房产抵押ID */
    private String mortgageId;
    /* 房产抵押编号 */
    private String mortgageCode;


    public static ApproveWarnMessageData create(String taskId, Loan loan) {
        ApproveWarnMessageData data = new ApproveWarnMessageData();
        data.setTaskId(taskId);
        data.setLoanId(loan.getId());
        data.setLoanCode(loan.getCode());
        data.setProductTypeId(loan.getProductTypeId());
        return data;
    }

    public static ApproveWarnMessageData create(String taskId, Mortgage mortgage) {
        ApproveWarnMessageData data = new ApproveWarnMessageData();
        data.setTaskId(taskId);
        data.setMortgageCode(mortgage.getMortgageCode());
        data.setMortgageId(mortgage.getId());
        return data;
    }

}

package com.kaisa.kams.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sunwanchao on 2016/12/24.
 * 还款记录表
 */
@Table("sl_loan_repay_record")
@Data
@NoArgsConstructor
public class LoanRepayRecord extends BaseModel{
    /**
     * 还款id
     */
    @Column("repayId")
    private String repayId;

    /**
     * 还款时间
     */
    @Column("repayDate")
    @ColDefine(type = ColType.DATE)
    private Date repayDate;

    /**
     * 还款本金
     */
    @Column("repayAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal repayAmount;

    /**
     * 还款利息
     */
    @Column("repayInterest")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal repayInterest;

    /**
     * 实际还款总金额(repayAmount+repayInterest)
     */
    @Column("repayTotalAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal repayTotalAmount;

    private List<LoanFeeRecord> loanFeeRecordList ;
    public void addLoanFeeRecord(LoanFeeRecord loanFeeRecord){
        if(loanFeeRecordList==null){
            loanFeeRecordList = new ArrayList<>();
        }
        this.loanFeeRecordList.add(loanFeeRecord);
    }

    private String repayments = "";
}

package com.kaisa.kams.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;

/**
 * Created by sunwanchao on 2016/12/28.
 * 费用记录表
 */
@Table("sl_loan_fee_record")
@Data
@NoArgsConstructor
public class LoanFeeRecord extends BaseModel{
    /**
     * 还款计划id
     */
    @Column("repayId")
    private String repayId;

    /**
     * 还款记录id
     */
    @Column("repayRecordId")
    private String repayRecordId;

    /**
     * 费用id
     */
    @Column("loanFeeId")
    private String loanFeeId;

    /**
     * 应还费用金额
     */
    @Column("feeAmount")
    @ColDefine(type = ColType.FLOAT, width = 16)
    private BigDecimal feeAmount;

    /**
     * 实还费用金额
     */
    @Column("repayFeeAmount")
    @ColDefine(type = ColType.FLOAT, width = 16)
    private BigDecimal repayFeeAmount;
}

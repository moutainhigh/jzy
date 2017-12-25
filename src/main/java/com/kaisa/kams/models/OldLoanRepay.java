package com.kaisa.kams.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

/**
 * Created by sunwanchao on 2017/4/18.
 */
@Table("sl_old_loan_repay")
@Data
@NoArgsConstructor
public class OldLoanRepay extends LoanRepay{
    @Column("repayId")
    private String repayId;

    /**
     * 第几次展期
     */
    @Column("position")
    @ColDefine(type = ColType.INT)
    private int position;
}

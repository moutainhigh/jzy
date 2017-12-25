package com.kaisa.kams.models;

import com.kaisa.kams.enums.FeeChargeNode;
import com.kaisa.kams.enums.FeeType;
import com.kaisa.kams.enums.LoanRepayStatus;
import com.kaisa.kams.enums.PublicStatus;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Index;
import org.nutz.dao.entity.annotation.Table;
import org.nutz.dao.entity.annotation.TableIndexes;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2016/11/24.
 * 贷款费用表
 */
@Table("sl_loan_fee")
@Data
@NoArgsConstructor
@TableIndexes({ @Index(name = "INDEX_LOAN_ID", fields = { "loanId" }, unique = false),@Index(name = "INDEX_REPAY_ID", fields = { "repayId" }, unique = false) })
public class LoanFee extends BaseModel{

    /**
     * 贷款id
     */
    @Column("loanId")
    private String loanId;

    /**
     * 费用Id
     */
    @Column("feeId")
    private String feeId;

    /**
     * 关联的还款计划Id
     */
    @Column("repayId")
    private String repayId;

    /**
     * 费用名称
     */
    @Column("feeName")
    @ColDefine(type = ColType.VARCHAR, width = 50)
    private String feeName;

    /**
     * 费用期数
     */
    @Column("period")
    @ColDefine(type = ColType.INT,width = 10)
    private int period;

    /**
     * 费用金额
     */
    @Column("feeAmount")
    @ColDefine(type = ColType.FLOAT, width = 16)
    private BigDecimal feeAmount;

    /**
     * 实际还款金额
     */
    @Column("repayFeeAmount")
    @ColDefine(type = ColType.FLOAT, width = 16)
    private BigDecimal repayFeeAmount;

    /**
     * 到期时间
     */
    @Column("dueDate")
    @ColDefine(type = ColType.DATE)
    private Date dueDate;

    /**
     * 还款时间
     */
    @Column("repayDate")
    @ColDefine(type = ColType.DATETIME)
    private Date repayDate;

    /**
     * 状态
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private LoanRepayStatus status;

    /**
     * 收取节点
     */
    @Column("chargeNode")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private FeeChargeNode chargeNode;

    /**
     * 费用类型
     */
    @Column("feeType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private FeeType feeType;

    /**
     * 历史费用金额
     */
    @Column("historyFeeAmount")
    @ColDefine(type = ColType.FLOAT, width = 16)
    private BigDecimal historyFeeAmount;
}

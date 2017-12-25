package com.kaisa.kams.models;

import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.enums.FeeType;
import com.kaisa.kams.enums.LoanRepayStatus;
import com.kaisa.kams.enums.PublicStatus;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Index;
import org.nutz.dao.entity.annotation.Table;
import org.nutz.dao.entity.annotation.TableIndexes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2016/11/24.
 * 还款计划表
 */
@Table("sl_loan_repay")
@Data
@NoArgsConstructor
@TableIndexes({ @Index(name = "INDEX_LOAN_ID", fields = { "loanId" }, unique = false) })
public class LoanRepay extends BaseModel{

    /**
     * 借款id
     */
    @Column("loanId")
    private String loanId;

    /**
     * 期数
     */
    @Column("period")
    private int period;

    /**
     * 需还总金额[需还本金+利息+费用] 精确到两位小数
     */
    @Column("totalAmount")
    @ColDefine(type = ColType.FLOAT,width=16)
    private BigDecimal totalAmount;

    /**
     * 实际还款总金额[实际还款本金+实际还款利息—+实际还款费用+其它费用] 精确到两位小数
     */
    @Column("repayTotalAmount")
    @ColDefine(type = ColType.FLOAT,width=16)
    private BigDecimal repayTotalAmount;

    /**
     * 需还本金 精确到两位小数
     */
    @Column("amount")
    @ColDefine(type = ColType.FLOAT,width=16)
    private BigDecimal amount;

    /**
     * 实还本金 精确到两位小数
     */
    @Column("repayAmount")
    @ColDefine(type = ColType.FLOAT,width=16)
    private BigDecimal repayAmount;

    /**
     * 需还利息 精确到两位小数
     */
    @Column("interest")
    @ColDefine(type = ColType.FLOAT,width=16)
    private BigDecimal interest;

    /**
     * 实还利息 精确到两位小数
     */
    @Column("repayInterest")
    @ColDefine(type = ColType.FLOAT,width=16)
    private BigDecimal repayInterest;

    /**
     * 需还费用总金额[不包含逾期费用，提前还款费用等其它费用]
     */
    @Column("feeAmount")
    @ColDefine(type = ColType.FLOAT,width=16)
    private BigDecimal feeAmount;

    /**
     * 实还费用总金额[不包含逾期费用，提前还款费用等其它费用]
     */
    @Column("repayFeeAmount")
    @ColDefine(type = ColType.FLOAT,width=16)
    private BigDecimal repayFeeAmount;

    /**
     * 需还其它费用
     */
    @Column("otherFee")
    @ColDefine(type = ColType.FLOAT,width=16)
    private BigDecimal otherFee;

    /**
     * 实还其它费用
     */
    @Column("repayOtherFee")
    @ColDefine(type = ColType.FLOAT,width=16)
    private BigDecimal repayOtherFee;

    /**
     * 其它费用类型
     */
    @Column("otherFeeType")
    @ColDefine(type = ColType.VARCHAR,width=40)
    private FeeType otherFeeType;

    /**
     * 到期时间
     */
    @Column("dueDate")
    @ColDefine(type = ColType.DATE)
    private Date dueDate;

    public String dueDate(){
        return DateUtil.formatDateToString(this.dueDate);
    }

    /**
     * 还款时间
     */
    @Column("repayDate")
    @ColDefine(type = ColType.DATETIME)
    private Date repayDate;

    /**
     * 详见LoanStatus
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private LoanRepayStatus status;

    /**
     * 剩余本金
     */
    @Column("outstanding")
    @ColDefine(type = ColType.FLOAT,width=16)
    private BigDecimal outstanding;

    /**
     * 备注
     */
    @Column("remark")
    @ColDefine(type = ColType.VARCHAR,width = 512)
    private String remark;

    /**
     * 逾期天数
     */
    @Column("overdueDays")
    @ColDefine(type = ColType.INT)
    private int overdueDays;


    private List<LoanRepayRecord> loanRepayRecordList;
    public void addLoanRepayRecord(LoanRepayRecord loanRepayRecord){
        if(loanRepayRecordList==null){
            loanRepayRecordList = new ArrayList<>();
        }
        loanRepayRecordList.add(loanRepayRecord);
    }
    private List<LoanFee> loanFeeList ;
    public void addLoanFee(LoanFee loanFee){
        if(loanFeeList==null){
            loanFeeList = new ArrayList<>();
        }
        loanFeeList.add(loanFee);
    }

    private int position;
    private String repayId;
}

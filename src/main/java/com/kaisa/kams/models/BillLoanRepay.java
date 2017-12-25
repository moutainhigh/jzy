package com.kaisa.kams.models;

import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.enums.RiskRank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by sunwanchao on 2017/2/23.
 * 票据还款计划表
 */
@Table("sl_bill_loan_repay")
@Data
@NoArgsConstructor
@TableIndexes({ @Index(name = "INDEX_REPAY_ID", fields = { "repayId" }, unique = false),@Index(name = "INDEX_LOAN_ID", fields = { "loanId" }, unique = false) })
public class BillLoanRepay extends BaseModel {

    @Column
    private String loanId;

    /**
     * 还款计划
     */
    @Column
    private String repayId;

    @One(target = LoanRepay.class, field = "repayId")
    private LoanRepay loanRepay;

    /**
     * 票号
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 120)
    private String billNo;

    /**
     * 出票日期
     */
    @Column
    @ColDefine(type = ColType.DATETIME)
    private Date drawTime;
    public String drawTime(){
        return DateUtil.formatDateToString(this.drawTime);
    }


    /**
     * 付款人ID
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String payerId;

    /**
     * 付款人
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String payer;

    /**
     * 付款人账号
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String payerAccount;

    /**
     * 风险等级
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 1)
    private RiskRank riskRank;

    /**
     * 收款人
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String payee;

    /**
     * 收款人账号
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String payeeAccount;

    /**
     * 收款人开户行
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String payeeBankName;

    /**
     * 付款人开户行
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String bankName;

    /**
     * 开户行地址
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 120)
    private String bankAddress;

    /**
     * 宽限期
     */
    @Column
    @ColDefine(type = ColType.INT,width = 10)
    private int overdueDays;

    /**
     * 排序位置
     */
    @Column
    @ColDefine(type = ColType.INT,width = 10)
    private int position;

    /**
     * 贴现日期
     */
    @Column
    @ColDefine(type = ColType.DATE)
    private Date disDate;

    /**
     * 实际贴现天数
     */
    @Column
    @ColDefine(type = ColType.INT,width = 10)
    private int disDays;

    /**
     * 实际到期日
     */
    @Column
    @ColDefine(type = ColType.DATE)
    private Date actualDueDate;

    /**
     * 成本报价利率
     */
    @Column
    @ColDefine(type = ColType.FLOAT,width=24, precision = 10)
    private BigDecimal costRate;

    /**
     * 居间费
     */
    @Column
    @ColDefine(type = ColType.FLOAT,width=24,precision = 10)
    private BigDecimal intermediaryFee;

    /**
     * 最小成本报价利率
     */
    @Column
    @ColDefine(type = ColType.FLOAT,width=24,precision = 10)
    private BigDecimal minCost;

    /**
     * 打款利率
     */
    @Column
    @ColDefine(type = ColType.FLOAT,width=24,precision = 10)
    private BigDecimal depositRate;

    /**
     * 打款标示
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width=10)
    private String depositFlag;


}

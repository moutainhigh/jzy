package com.kaisa.kams.models;

import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.TextFormatUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by sunwanchao on 2017/2/23.
 * 票据信息表
 */
@Table("sl_bill_loan")
@Data
@NoArgsConstructor
@TableIndexes({ @Index(name = "INDEX_LOAN_ID", fields = { "loanId" }, unique = false) })
public class BillLoan extends BaseModel {
    /**
     * 标的信息
     */
    @Column
    private String loanId;

    @One(target = Loan.class, field = "loanId")
    private Loan loan;

    /**
     * 票面总金额
     */
    @Column
    @ColDefine(type = ColType.FLOAT, width = 16)
    private BigDecimal totalAmount;

    /**
     * 贴现利息
     */
    @Column
    @ColDefine(type = ColType.FLOAT, width = 16)
    private BigDecimal interest;

    /**
     * 贴现日期
     */
    @Column
    @ColDefine(type = ColType.DATETIME)
    private Date discountTime;
    public String discountTime(){
        return DateUtil.formatDateToString(this.discountTime);
    }

    /**
     * 户名
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String accountName;

    /**
     * 开户行
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String accountBank;

    /**
     * 帐号
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width = 40)
    private String accountNo;

//    /**
//     * 居间人
//     */
//    @Column
//    @ColDefine(type = ColType.VARCHAR, width = 50)
//    private String intermediaryName;

    /**
     * 居间人id
     */
    @Column("intermediaryId")
    private String intermediaryId;

    /**
     * 居间人名称
     */
    @Column("name")
    @ColDefine(type=ColType.VARCHAR,width=20)
    private String name;

    /**
     * 身份证号码
     */
    @Column("idNumber")
    @ColDefine(type=ColType.VARCHAR,width=100)
    private String idNumber;

    /**
     * 电话
     */
    @Column("phone")
    @ColDefine(type=ColType.VARCHAR,width=20)
    private String phone;

    /**
     * 家庭住址
     */
    @Column("address")
    @ColDefine(type = ColType.VARCHAR,width=256)
    private String address;

    /**
     * 开户行
     */
    @Column("bank")
    @ColDefine(type=ColType.VARCHAR,width=100)
    private String bank;

    /**
     * 账户
     */
    @Column("account")
    @ColDefine(type = ColType.VARCHAR,width = 30)
    private String account;

    /**
     * 居间总费用
     */
    @Column
    @ColDefine(type = ColType.FLOAT, width = 24,precision = 10)
    private BigDecimal intermediaryTotalFee;

    /**
     * 代扣代缴税费
     */
    @Comment("代扣代缴税费")
    @Column
    @ColDefine(type = ColType.FLOAT, width = 24,precision = 2)
    private BigDecimal withHoldingTaxFee;

    /**
     * 税后居间费
     */
    @Comment("税后居间费")
    @Column
    @ColDefine(type = ColType.FLOAT, width = 24,precision = 2)
    private BigDecimal afterTaxIntermediaryFee;

    public String getAccountNo(){
        return TextFormatUtils.formatAccount(this.accountNo);
    }
}

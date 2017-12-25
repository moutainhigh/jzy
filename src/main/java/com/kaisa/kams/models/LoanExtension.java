package com.kaisa.kams.models;

import com.kaisa.kams.enums.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;

/**
 * Created by sunwanchao on 2017/4/11.
 * 业务展期表
 */
@Table("sl_loan_extension")
@Data
@NoArgsConstructor
public class LoanExtension extends BaseModel {
    /**
     * 标的id
     */
    @Column("loanId")
    private String loanId;

    /**
     * 期限类型
     */
    @Column("termType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private LoanTermType termType;

    /**
     * 期限
     */
    @Column("term")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String term;

    /**
     * 利息类型
     */
    @Column("loanLimitType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private LoanLimitType loanLimitType;

    /**
     * 固定金额
     */
    @Column("interestAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private  BigDecimal interestAmount;

    /**
     * 利率
     */
    @Column("interestRate")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal interestRate;

    /**
     * 展期说明
     */
    @Column("enterpriseExplain")
    @ColDefine(type = ColType.VARCHAR, width=300)
    private String enterpriseExplain;

    /**
     * 展期协议
     */
    @Column("enterpriseAgreement")
    @ColDefine(type = ColType.TEXT)
    private String enterpriseAgreement;

    /**
     * 第几次展期
     */
    @Column("position")
    @ColDefine(type = ColType.INT)
    private int position;

    /**
     * 还款方式
     */
    @Column("repayMethod")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String repayMethod;

    /**
     * 计算方式
     */
    @Column("calculationMethod")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String calculationMethod;

    /**
     * 收息时间
     */
    @Column("repayDateType")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String repayDateType;

    /**
     * 展期ID
     */
    @Column("extensionId")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String extensionId ;

    private ApprovalStatusType approvalStatusType;

    private String extensionCode;


}

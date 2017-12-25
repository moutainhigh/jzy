package com.kaisa.kams.models;

import com.kaisa.kams.components.utils.excelUtil.Condition;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.flow.FlowControlTmpl;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.math.BigDecimal;

/**
 * Created by zhouchuang on 2017/10/31.
 */
@Table("sl_extension")
@Data
@NoArgsConstructor
public class Extension  extends BaseApproval{

    private LoanExtension loanExtension;

    @Condition(condition = "LIKE",sql="%{}%")
    @Column("extensionCode")
    @Comment("展期编号")
    @ColDefine(type = ColType.VARCHAR,width = 64)
    private String extensionCode;

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



    private String businessSource;
    private String productTypeName;
    private String borrowerName;
    private LoanStatus loanStatus;



    private String code;
    private String extensionId;


}

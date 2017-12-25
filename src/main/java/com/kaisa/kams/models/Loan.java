package com.kaisa.kams.models;

import com.kaisa.kams.components.utils.DecimalFormatUtils;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.business.BusinessUser;
import org.nutz.dao.entity.annotation.*;

import java.math.BigDecimal;
import java.util.Date;


import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2016/11/24.
 * 贷款申请表
 */
@Table("sl_loan")
@Data
@NoArgsConstructor
@TableIndexes({ @Index(name = "INDEX_LOAN_CODE", fields = { "code" }, unique=true) })
public class Loan extends BaseModel{

    /**
     * 产品类型Id
     */
    @Column("productTypeId")
    private String productTypeId;

    /**
     * 产品类型对象实例
     */
    @One(target = ProductType.class, field = "productTypeId")
    private ProductType productType;

    /**
     * 产品Id
     */
    @Column("productId")
    private String productId;

    @One(target = Product.class, field = "productId")
    private Product product;


    /**
     * 放款主体
     */
    @Column("loanSubjectId")
    private String loanSubjectId;

    /**
     * 放款主体帐号
     */
    @Column("loanSubjectAccountId")
    private String loanSubjectAccountId;


    /**
     * 主借款人id
     */
    @Column("masterBorrowerId")
    private String masterBorrowerId;

    /**
     * 主借款人信息
     */
    @One(target =LoanBorrower.class,field ="masterBorrowerId" )
    private LoanBorrower masterBorrower;



    /**
     * 申请金额
     */
    @Column("amount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal amount;

    /**
     * 实际放款金额
     */
    @Column("actualAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal actualAmount;

    /**
     * 还款方式
     */
    @Column("repayMethod")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private LoanRepayMethod repayMethod;

    /**
     * 利息类型
     */
    @Column("loanLimitType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private LoanLimitType loanLimitType;

    public String loanLimitType(){
        if (LoanLimitType.FIX_AMOUNT.equals(loanLimitType)) {
            return "（金额计息）";
        } else if (LoanLimitType.FIX_RATE.equals(loanLimitType)) {
            return "（比例计息）";
        } else {
            return  "- -";
        }
    }

    /**
     * 还款时间类型
     */
    @Column("repayDateType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private LoanRepayDateType repayDateType;

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

    public String getPercentageInterestRate(){
        if(LoanLimitType.FIX_AMOUNT.equals(loanLimitType)){
            return DecimalFormatUtils.removeZeroFormat(this.interestAmount)+"元";
        }else {
            return DecimalFormatUtils.removeZeroFormat(this.interestRate)+"%";
        }
    }

    /**
     * 审批状态
     */
    @Column("approveStatus")
    @ColDefine(type = ColType.VARCHAR, width=120)
    private String approveStatus;

    /**
     * 审批状态描述
     */
    @Column("approveStatusDesc")
    @ColDefine(type = ColType.VARCHAR, width=120)
    private String approveStatusDesc;

    /**
     * 单状态
     */
    @Column("loanStatus")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private LoanStatus loanStatus;

    /**
     * 状态
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private PublicStatus status;

    /**
     * 业务员Id
     */
    @Column("saleId")
    private String saleId;

    /**
     * 业务员姓名
     */
    @Column("saleName")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String saleName;

    /**
     * 承做业务员Id
     */
    @Column("engagedSaleId")
    private String engagedSaleId;

    /**
     * 承做业务员姓名
     */
    @Column("engagedSaleName")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String engagedSaleName;

    /**
     * 业务员
     */
    @One(target = BusinessUser.class, field = "saleId")
    private BusinessUser sale;

    /**
     * 提交时间
     */
    @Column("submitTime")
    @ColDefine(type = ColType.DATETIME)
    private Date submitTime;

    /**
     * 业务员编码
     */
    @Column("saleCode")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String saleCode;

    /**
     * 业务单号
     */
    @Column("code")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String code;

    /**
     * 期限类型
     */
    @Column("termType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private LoanTermType termType;



    public String termType(){
        return termType.equals(LoanTermType.FIXED_DATE)?"":termType.getDescription().substring(1,2);
    }
    public String termType1(){
        return termType.equals(LoanTermType.FIXED_DATE)?"天":termType.getDescription().substring(1,2);
    }
    public String termType2(){
        return termType.equals(LoanTermType.FIXED_DATE)?"至"+term:term+termType();
    }

    /**
     * 期限
     */
    @Column("term")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String term;

    /**
     * 申请人
     */
    @Column("applyId")
    private String applyId;

    /**
     * 放款日期
     */
    @Column("loanTime")
    @ColDefine(type = ColType.DATETIME)
    private Date loanTime;

    @Column("step")
    private String step;

    /**
     * 最小利息金额
     */
    @Column("minInterestAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private  BigDecimal minInterestAmount;

    /**
     * 按天计息 头尾计算规则
     */
    @Column("calculateMethodAboutDay")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private CalculateMethodAboutDay calculateMethodAboutDay;

    /**
     * 渠道Id
     */
    @Column("channelId")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String channelId;

    /**
     * 要件控制状态
     */
    @Column("elementStatus")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String elementStatus;

    /**
     * 操作要件控制状态的操作人
     */
    @Column("elementConfirmName")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String elementConfirmName;

    /**
     *  操作要件控制状态时间
     */
    @Column("elementConfirmTime")
    @ColDefine(type = ColType.DATETIME)
    private Date elementConfirmTime;

    /**
     *
     * 历史数据导入 01历史数据导入
     */
    @Column("historyData")
    @ColDefine(type = ColType.VARCHAR, width=2)
    private String historyData="00";

    /**
     *
     * 真实业务员名称
     */
    @Column("actualBusinessName")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String actualBusinessName;

    /**
     * 宽限天数
     */
    @Column("grace")
    @ColDefine(type = ColType.INT,width = 10)
    private int grace;

    /**
     * 结清日期
     */
    @Column("clearDate")
    @ColDefine(type = ColType.DATE)
    private Date clearDate;


    /**
     * 来源
     */
    @Column("source")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private SourceType source;

    /**
     * 渠道申请人
     */
    @Column("channelApplyId")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String channelApplyId;

    /**
     * 关联类型
     */
    @Column("associationType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private associationType associationType;

}

package com.kaisa.kams.models;

import com.kaisa.kams.enums.PublicStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;

/**
 * Created by zhouchuang on 2017/7/31.
 */
@Table("sl_loan_profit")
@Data
@NoArgsConstructor
public class LoanProfit  extends BaseModel {



    /**
     *loanID
     */
    @Column("loanId")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String loanId;


    /**
     * 详见PublicStatus
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private PublicStatus status;

    /**
     * 利息收入
     */
    @Column("interestRevenue")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 6)
    private BigDecimal interestRevenue;

    /**
     * 附加税
     */
    @Column("surtax")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 6)
    private BigDecimal surtax;


    /**
     * 人工费
     */
    @Column("laborCost")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 6)
    private BigDecimal laborCost;


    /**
     * 行政费
     */
    @Column("administrativeExpenses")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 6)
    private BigDecimal administrativeExpenses;


    /**
     * 居间费
     */
    @Column("brokerageFee")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 6)
    private BigDecimal brokerageFee;
    /**
     * 资产坏账拨备
     */
    @Column("badAssetsReserve")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 6)
    private BigDecimal badAssetsReserve;
    /**
     * 金服运营成本
     */
    @Column("operatingCost")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 6)
    private BigDecimal operatingCost;

    /**
     * 资金成本
     */
    @Column("capitalCost")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 6)
    private BigDecimal capitalCost;



    /**
     * 增值税
     */
    @Column("valueAddedTax")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 6)
    private BigDecimal valueAddedTax;

    /**
     * 利润
     */
    @Column("profit")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 6)
    private BigDecimal profit;

}

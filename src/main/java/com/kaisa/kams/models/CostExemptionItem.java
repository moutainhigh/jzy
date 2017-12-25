package com.kaisa.kams.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.math.BigDecimal;

/**
 * @description：费用免除详情类
 * @author：zhouchuang
 * @date：2017-11-14:48
 */
@Table("sl_cost_exemption_item")
@Data
@NoArgsConstructor
public class CostExemptionItem extends BaseModel {
    @Comment("费用减免订单id")
    @Column("costExemptionId")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String costExemptionId;

    @Comment("借款服务费")
    @Column("serviceFee")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal serviceFee;

    @Comment("原借款服务费")
    @Column("serviceFeePre")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal serviceFeePre;

    @Comment("借款担保费")
    @Column("guaranteeFee")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal guaranteeFee;

    @Comment("原借款担保费")
    @Column("guaranteeFeePre")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal guaranteeFeePre;


    @Comment("资金管理费")
    @Column("manageFee")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal manageFee;

    @Comment("原资金管理费")
    @Column("manageFeePre")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal manageFeePre;

    @Comment("逾期罚息")
    @Column("overdueFee")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal overdueFee;

    @Comment("原逾期罚息")
    @Column("overdueFeePre")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal overdueFeePre;

    @Comment("提前结清罚息")
    @Column("prepaymentFee")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal prepaymentFee;

    @Comment("原提前结清罚息")
    @Column("prepaymentFeePre")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal prepaymentFeePre;

    @Comment("一次性服务费")
    @Column("prepaymentFeeRate")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal prepaymentFeeRate;

    @Comment("原一次性服务费")
    @Column("prepaymentFeeRatePre")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal prepaymentFeeRatePre;


    @Comment("利息")
    @Column("interest")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal interest;

    @Comment("原利息")
    @Column("interestPre")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal interestPre;


    @Comment("期数")
    @Column("period")
    private int period;

    @Comment("还款计划Id")
    @Column("repayId")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String repayId;

    @Comment("费用减免原因")
    @Column("exemptionReason")
    @ColDefine(type = ColType.VARCHAR, width=1024)
    private String exemptionReason;

}

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
@Table("sl_product_profit")
@Data
@NoArgsConstructor
public class ProductProfit  extends BaseModel{

    /**
     *产品大类ID
     */
    @Column("productTypeId")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String productTypeId;

    /**
     * 详见PublicStatus
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private PublicStatus status;

    private String name;
    private String guarantyType;
    private String businessType;
    private String code;
    private Integer operation;//0 表示没有数据，可新增，1表示有数据，可修改
    private String type;  //大类的code类型

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
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 10)
    private BigDecimal surtax;


    /**
     * 人工费自营
     */
    @Column("laborCostSelf")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 6)
    private BigDecimal laborCostSelf;
    /**
     * 人工费渠道
     */
    @Column("laborCostChannel")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 6)
    private BigDecimal laborCostChannel;

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
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 10)
    private BigDecimal brokerageFee;
    /**
     * 资产坏账拨备
     */
    @Column("badAssetsReserve")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 10)
    private BigDecimal badAssetsReserve;
    /**
     * 金服运营成本（月）
     */
    @Column("operatingCostMonth")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 10)
    private BigDecimal operatingCostMonth;
    /**
     * 金服运营成本（日）
     */
    @Column("operatingCostDay")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 10)
    private BigDecimal operatingCostDay;

    /**
     * 资金成本（日）
     */
    @Column("capitalCostDay")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 10)
    private BigDecimal capitalCostDay;

    /**
     * 资金成本（月）
     */
    @Column("capitalCostMonth")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 10)
    private BigDecimal capitalCostMonth;

    /**
     * 税价合计系数
     */
    @Column("totalTax")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 6)
    private BigDecimal totalTax;

    /**
     * 增值税
     */
    @Column("valueAddedTax")
    @ColDefine(type = ColType.FLOAT, width = 16,precision = 10)
    private BigDecimal valueAddedTax;

}

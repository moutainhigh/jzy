package com.kaisa.kams.models;

import com.kaisa.kams.enums.CompanyType;
import com.kaisa.kams.enums.CreditRating;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by liuwen01 on 2016/12/14.
 * 用印管理表
 */
@Table("sl_enterprise")
@Data
@NoArgsConstructor
public class Enterprise extends BaseModel{

    /**
     * 名称
     */
    @Column("name")
    @ColDefine(type=ColType.VARCHAR,width=20)
    private String name;


    /**
     * 资信等级
     */
    @Column("level")
    @ColDefine(type=ColType.VARCHAR,width=20)
    private CreditRating level;

    /**
     * 底价
     */
    @Column("price")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal price;

    /**
     * 成立时间
     */
    @Column("establishDate")
    private  Date establishDate;

    /**
     * 所有制性质
     */
    @Column("nature")
    @ColDefine(type=ColType.VARCHAR,width=20)
     private String nature;

    /**
     * 公司性质
     */
    @Column("computerNature")
    @ColDefine(type=ColType.VARCHAR,width=20)
    private String computerNature;

    /**
     * 授信额度
     */
    @Column("creditQuota")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal creditQuota;

    /**
     * 年度
     */
    @Column("year")
    @ColDefine(type=ColType.VARCHAR,width=20)
    private String year;

    /**
     * 营业收入
     */
    @Column("businessIncome")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal businessIncome;

    /**
     * 扣非净利润
     */
    @Column("notNetProfit")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal notNetProfit;


    /**
     * 经营性现金流净额
     */

    @Column("cashFlowNet")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal  cashFlowNet;

    /**
     * 资产负债率
     */
    @Column("assetsDebtRatio")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal  assetsDebtRatio;


    /**
     * 公司基本情况
     */
    @Column("computerBasic")
    @ColDefine(type = ColType.VARCHAR, width=300)
    private String computerBasic;

    /**
     * 失信执行人情况
     */
    @Column("executorSituation")
    @ColDefine(type = ColType.VARCHAR, width=200)
    private String executorSituation;

    /**
     *  纠纷
     */
    @Column("dispute")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String dispute;


    /**
     * 经办人意见
     */
    @Column("managerOpinion")
    @ColDefine(type = ColType.VARCHAR, width=200)
    private String managerOpinion;


    /**
     * 其他情况
     */
    @Column("otherSituation")
    @ColDefine(type = ColType.VARCHAR, width=200)
    private String otherSituation;

    /**
     * 公司类型
     */
    @Column("type")
    @ColDefine(type=ColType.VARCHAR,width=20)
    private CompanyType type;

    /**
     * 剩余额度
     */
    @Column("remainderAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal remainderAmount;

    /**
     * 在库额度
     */
    @Column("libraryAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal libraryAmount;

    /**
     * 关联授信公司
     */
    @Column("companyId")
    @ColDefine(type = ColType.VARCHAR, width=256)
    private String companyId;

    /**
     * 关联授信公司名称
     */
    @Column("companyName")
    @ColDefine(type = ColType.VARCHAR, width=150)
    private String companyName;

    /**
     * 子公司合集
     */
    @Column("companies")
    @ColDefine(type = ColType.VARCHAR, width=1024)
    private String companies;

}

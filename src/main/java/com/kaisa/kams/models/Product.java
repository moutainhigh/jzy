package com.kaisa.kams.models;

import com.kaisa.kams.enums.PublicStatus;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2016/11/24.
 * 产品表
 */
@Table("sl_product")
@Data
@NoArgsConstructor
public class Product extends BaseModel{
    /**
     * 产品编号 前两位使用大类 后六位由用户输入
     */
    @Column("code")
    @ColDefine(type = ColType.CHAR, width=8)
    private String code;

    /**
     * 产品大类Id
     */
    @Column("typeId")
    private String typeId;

    /**
     * 产品大类
     */
    @One(target = ProductType.class, field ="typeId" )
    private ProductType productType;

    /**
     * 产品名称
     */
    @Column("name")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String name;

    /**
     * 产品别名
     */
    @Column("alias")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String alias;

    /**
     * 产品说明
     */
    @Column("description")
    @ColDefine(type = ColType.VARCHAR, width=300)
    private String description;

    /**
     * 借款期限（二进制字符天月固定时间）
     */
    @Column("loanTermType")
    @ColDefine(type = ColType.VARCHAR, width=10)
    private String loanTermType;

    /**
     * 期限月最小
     */
    @Column("minMonths")
    @ColDefine(type = ColType.INT,width = 10)
    private int minMonths;

    /**
     * 期限月最大 最大为36个月
     */
    @Column("maxMonths")
    @ColDefine(type = ColType.INT,width = 10)
    private int maxMonths;

    /**
     * 期限天最小
     */
    @Column("minDays")
    @ColDefine(type = ColType.INT,width = 10)
    private int minDays;

    /**
     * 期限天最大 最大天1000
     */
    @Column("maxDays")
    @ColDefine(type = ColType.INT,width = 10)
    private int maxDays;

    /**
     * 期限季最小
     */
    @Column("minSeasons")
    @ColDefine(type = ColType.INT,width = 10)
    private int minSeasons;

    /**
     * 期限季最大 最大季度4
     */
    @Column("maxSeasons")
    @ColDefine(type = ColType.INT,width = 10)
    private int maxSeasons;

    /**
     * 还款方式（二进制字符  先息后本 等额本息 一次性还本付息）
     */
    @Column("repayMethod")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String repayMethod;


    /**
     * 还款时间类型 （二进制字符，放款日还款（期初），放款日还款（期末））
     */
    @Column("repayDateType")
    @ColDefine(type = ColType.VARCHAR, width=10)
    private String repayDateType;

    /**
     * 计息方式（二进制字符 固定费用，固定费率)
     */
    @Column("interestType")
    @ColDefine(type = ColType.VARCHAR, width=10)
    private String interestType;

    /**
     * 计息固定费用
     */
    @Column("interestAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal interestAmount;

    /**
     * 计息最小费用
     */
    @Column("minInterestAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal minInterestAmount;

    /**
     * 计息固定费用利率
     */
    @Column("interestRate")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal interestRate;

    /**
     * 计息最小利率
     */
    @Column("minInterestRate")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal minInterestRate;

    /**
     * 逾期宽限天数
     */
    @Column("overdueDays")
    @ColDefine(type = ColType.INT,width = 10)
    private int overdueDays;

    /**
     * 产品限额最小值
     */
    @Column("minAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal minAmount;

    /**
     * 产品限额最大值
     */
    @Column("maxAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal maxAmount;

    /**
     * 还款提前通知天数
     */
    @Column("repayNotifyEarlyDays")
    @ColDefine(type = ColType.INT,width = 10)
    private int repayNotifyEarlyDays;

    /**
     * 是否短信通知
     */
    @Column("isSendSms")
    @ColDefine(type = ColType.BOOLEAN)
    private boolean isSendSms;

    /**
     * 产品模板Id
     */
    @Column("infoTmpId")
    private String infoTmpId;

    /**
     * 影像资料模板Id
     */
    @Column("mediaTmpId")
    private String mediaTmpId;

    /**
     * 产品模板
     */
    @One(target = ProductInfoTmpl.class, field ="infoTmpId" )
    private ProductInfoTmpl productInfoTmpl;

    /**
     * 影像资料模板
     */
    @One(target = ProductMediaTmpl.class, field ="mediaTmpId" )
    private ProductMediaTmpl mediaTmpl;

    /**
     * 有效状态
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR,width=40)
    private PublicStatus status;

    /**
     * 产品流程配置是否完整
     */
    @Column("flowConfigStatus")
    @ColDefine(type = ColType.BOOLEAN)
    private boolean flowConfigStatus;

    /**
     * 流程规则Id
     */
    @Column("processId")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String processId;


    /**
     * 借款期限为天时 计息方式（二进制字符 固定费用，固定费率)
     */
    @Column("dayInterestType")
    @ColDefine(type = ColType.VARCHAR, width=10)
    private String dayInterestType;

    /**
     * 计息固定费用
     */
    @Column("dayInterestAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal dayInterestAmount;

    /**
     * 计息最小费用
     */
    @Column("dayMinInterestAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal dayMinInterestAmount;

    /**
     * 计息固定费用利率
     */
    @Column("dayInterestRate")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal dayInterestRate;

    /**
     * 计息最小利率
     */
    @Column("dayMinInterestRate")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal dayMinInterestRate;

    /**
     * 借款期限为月时 计息方式（二进制字符 固定费用，固定费率)
     */
    @Column("monthInterestType")
    @ColDefine(type = ColType.VARCHAR, width=10)
    private String monthInterestType;

    /**
     * 计息固定费用
     */
    @Column("monthInterestAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal monthInterestAmount;

    /**
     * 计息最小费用
     */
    @Column("monthMinInterestAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal monthMinInterestAmount;

    /**
     * 计息固定费用利率
     */
    @Column("monthInterestRate")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal monthInterestRate;

    /**
     * 计息最小利率
     */
    @Column("monthMinInterestRate")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal monthMinInterestRate;


    /**
     * 借款期限为季时 计息方式（二进制字符 固定费用，固定费率)
     */
    @Column("seasonInterestType")
    @ColDefine(type = ColType.VARCHAR, width=10)
    private String seasonInterestType;

    /**
     * 计息固定费用
     */
    @Column("seasonInterestAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal seasonInterestAmount;

    /**
     * 计息最小费用
     */
    @Column("seasonMinInterestAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal seasonMinInterestAmount;

    /**
     * 计息固定费用利率
     */
    @Column("seasonInterestRate")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal seasonInterestRate;

    /**
     * 计息最小利率
     */
    @Column("seasonMinInterestRate")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal seasonMinInterestRate;


}

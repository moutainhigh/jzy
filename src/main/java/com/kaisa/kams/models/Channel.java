package com.kaisa.kams.models;

/**
 * Created by pengyueyang on 2017/3/1.
 */

import com.kaisa.kams.enums.PublicStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by pengyueyang on 2016/11/25.
 * 渠道信息
 */
@Table("sl_channel")
@Data
@NoArgsConstructor
public class Channel extends BaseModel{

    /**
     * 渠道简称
     */
    @Column("name")
    @ColDefine(type = ColType.VARCHAR,width = 60)
    private String name;

    /**
     * 渠道代码
     */
    @Column("code")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String code;

    /**
     * 渠道全称
     */
    @Column("fullName")
    @ColDefine(type = ColType.VARCHAR,width = 60)
    private  String fullName;

    /**
     * 渠道经理
     */
    @Column("manager")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String manager;

    /**
     * 合作金额
     */
    @Column("cooperationAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal cooperationAmount;

    /**
     * 在库金额
     */
    @Column("residualAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private  BigDecimal residualAmount;

    /**
     * 保证金金额
     */
    @Column("guaranteeAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private  BigDecimal guaranteeAmount;

    /**
     * 风险措施
     */
    @Column("riskControlMeasure")
    @ColDefine(type = ColType.VARCHAR, width=300)
    private String riskControlMeasure;


    /**
     * 合作产品类型
     */
    @Column("cooperationProductType")
    @ColDefine(type = ColType.TEXT)
    private String cooperationProductType;

    /**
     * 渠道类型：0:自营 1:渠道
     */
    @Column("channelType")
    @ColDefine(type = ColType.VARCHAR,width = 10)
    private String channelType;

    /**
     * 联系方式
     */
    @Column("contactWay")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private  String contactWay;

    /**
     * 渠道对接人
     */
    @Column("buttMan")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private  String buttMan;

    /**
     * 合同名称
     */
    @Column("contractFileName")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private  String contractFileName;

    /**
     * 合同地址
     */
    @Column("contractFileUrl")
    @ColDefine(type = ColType.VARCHAR,width = 256)
    private  String contractFileUrl;

    /**
     * 是否启用
     * 详见PublicStatus
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private PublicStatus status;


    /**
     * 渠道经理Id
     */
    @Column("managerId")
    private String managerId;


    /**
     * 生效日期
     */
    @Column("effectiveDate")
    private Date effectiveDate;

    /**
     * 利率
     */
    @Column("interestRate")
    @ColDefine(type = ColType.FLOAT, width=24,precision = 10)
    private BigDecimal interestRate;

    /**
     * 支持多协议保存urls
     */
    @Column("contractFileUrls")
    @ColDefine(type = ColType.TEXT)
    private String contractFileUrls;



    /**
     * 保证金杠杆额度
     */
    @Column("depositLimit")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal depositLimit;
}

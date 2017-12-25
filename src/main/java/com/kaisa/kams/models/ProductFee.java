package com.kaisa.kams.models;

import com.kaisa.kams.enums.FeeChargeNode;
import com.kaisa.kams.enums.FeeChargeType;
import com.kaisa.kams.enums.FeeCycleType;
import com.kaisa.kams.enums.FeeType;
import com.kaisa.kams.enums.LoanRepayMethod;
import com.kaisa.kams.enums.PublicStatus;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2016/11/24.
 * 产品费用表
 */
@Table("sl_product_fee")
@Data
@NoArgsConstructor
public class ProductFee extends BaseModel{

    /**
     * 费用编号 FY+产品大类+产品编码+001递增
     */
    @Column("code")
    @ColDefine(type = ColType.VARCHAR, width=20)
    private String code;

    /**
     * 产品id
     */
    @Column("productId")
    private String productId;

    /**
     * 费用类型
     */
    @Column("feeType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private FeeType feeType;

    /**
     * 费用频率
     */
    @Column("feeCycle")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private FeeCycleType feeCycle;

    /**
     * 收取方式
     */
    @Column("chargeType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private FeeChargeType chargeType;

    /**
     * 收取节点
     */
    @Column("chargeNode")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private FeeChargeNode chargeNode;

    /**
     * 固定金额或者比例
     */
        @Column("chargeBase")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal chargeBase;

    /**
     * 最低费用
     */
    @Column("minFeeAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal minFeeAmount;

    /**
     * 最高费用
     */
    @Column("maxFeeAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal maxFeeAmount;

    /**
     * 还款方式
     */
    @Column("repayMethod")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private LoanRepayMethod repayMethod;

    /**
     * 渠道类型：ALL:全部 0:自营 1:渠道
     */
    @Column("channelType")
    @ColDefine(type = ColType.VARCHAR,width = 10)
    private String channelType;
}

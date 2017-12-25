package com.kaisa.kams.models;

import com.kaisa.kams.enums.FeeChargeNode;
import com.kaisa.kams.enums.FeeChargeType;
import com.kaisa.kams.enums.FeeCycleType;
import com.kaisa.kams.enums.FeeType;
import com.kaisa.kams.enums.LoanRepayMethod;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2016/11/24.
 * 贷款申请费用临时表
 */
@Table("sl_loan_fee_temp")
@Data
@NoArgsConstructor
public class LoanFeeTemp extends BaseModel{

    /**
     * loanId
     */
    @Column("loanId")
    private String loanId;

    /**
     * 费用名称
     */
    @Column("feeName")
    @ColDefine(type = ColType.VARCHAR, width=50)
    private String feeName;

    /**
     * 费用公式名称
     */
    @Column("formulaText")
    @ColDefine(type = ColType.VARCHAR, width=254)
    private String formulaText;

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
     * 费用比例
     */
    @Column("feeRate")
    @ColDefine(type = ColType.FLOAT, width=24, precision = 10)
    private BigDecimal feeRate;

    /**
     * 费用金额（收费方式为固定金额，或者前端输入时需要）
     */
    @Column("feeAmount")
    @ColDefine(type = ColType.FLOAT, width=16)
    private BigDecimal feeAmount;
    public String feeAmount(){
        if(feeAmount==null||feeAmount.doubleValue()==0D){
            return "- -";
        }else{
            BigDecimal setScale = feeAmount.setScale(2,   BigDecimal.ROUND_HALF_DOWN);
            return setScale.toString();
        }
    }

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



    public LoanFeeTemp(ProductFee productFee) {
        this.chargeNode = productFee.getChargeNode();
        this.feeType = productFee.getFeeType();
        this.feeCycle = productFee.getFeeCycle();
        this.chargeType = productFee.getChargeType();
        this.minFeeAmount = productFee.getMinFeeAmount();
        this.maxFeeAmount = productFee.getMaxFeeAmount();
        if(productFee.getChargeType()==FeeChargeType.FIXED_AMOUNT||productFee.getChargeType()==FeeChargeType.LOAN_REQUEST_INPUT){
            this.feeAmount = productFee.getChargeBase();
            this.feeRate = new BigDecimal(0);
        }else {
            this.feeRate = productFee.getChargeBase();
        }
        this.repayMethod = productFee.getRepayMethod();
    }

}
